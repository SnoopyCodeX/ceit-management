<?php

class _Parent {
	private static $response;
	private static $table = 'parents';
	private static $conn;
	
	public function __construct($db)
	{
		self::$conn = $db;
		self::$response = (object) array();
	}
	
	public static function init($db)
	{
		return new _Parent($db);
	}
	
	public static function get(int $id = -1)
	{
		if($id == -1)
			$query = "SELECT * FROM " . self::$table . " WHERE deleted='0'";
		else
			$query = "SELECT * FROM " . self::$table . " WHERE id=('$id')";
		
		$res = self::$conn->query($query);
		
		if($res)
		{
			if($res->num_rows > 0)
			{
				$data = array();

				while($row = $res->fetch_assoc())
				{
					$row['gender'] = $row['gender'] == 0 ? 'Female' : 'Male';

					$birthday = $row['birthday'];
					$date = date_create($birthday, timezone_open('Asia/Manila'));
					$date = date_format($date, 'M d, Y');
					$row['birthday'] = $date;

					$query = "SELECT * FROM students WHERE parent='" . $row['id'] . "'";
					$res1 = self::$conn->query($query);

					if(!$res1)
					{
						self::$response->hasError = !$res1;
						self::$response->message = self::$conn->error;
						return self::$response;
					}
					else if($res1->num_rows > 0)
					{
						$selectedChild = array();
						
						while($row1 = $res1->fetch_assoc())
						{
							$query = "SELECT * FROM classes WHERE id='" . $row1['section'] . "'";
							$query = self::$conn->query($query);

							if($query && $query->num_rows > 0)
							{
								$class = $query->fetch_assoc();
								$section = $class['name'];
								$row1['section'] = $section;
							}
							else
								$row1['section'] = "No class specified";

							$row1['gender'] = $row1['gender'] == 0 ? 'Female' : 'Male';

							$birthday = $row1['birthday'];
							$date = date_create($birthday, timezone_open('Asia/Manila'));
							$date = date_format($date, 'M d, Y');
							$row1['birthday'] = $date;

							array_push($selectedChild, $row1);
						}

						$row['selectedChild'] = $selectedChild;
					}

					array_push($data, $row);
				}
				
				self::$response->hasError = false;
				self::$response->data = $data;
			}
			else
			{
				self::$response->hasError = !$res;
				self::$response->message = 'No data was found on the database';
			}
		}
		else
		{
			self::$response->hasError = !$res;
			self::$response->message = self::$conn->error;
		}

		return self::$response;
	}

	public static function getAllRemoved()
	{
		$query = "SELECT * FROM " . self::$table . " WHERE deleted='1'";
		$res = self::$conn->query($query);

		if($res && $res->num_rows > 0)
		{
			$data = array();
				
			while($row = $res->fetch_assoc())
			{
				$row['gender']   = $row['gender'] == 0 ? 'Female' : 'Male';
				array_push($data, $row);
			}

			self::$response->hasError = false;
			self::$response->data = $data;
		}
		else
		{
			self::$response->hasError = !$res;
			self::$response->message = $res ? 'No data was found on the database' : self::$conn->error;
		}

		return self::$response;
	}
	
	public static function update(int $id, object $data)
	{
		$user = self::get($id);
		
		if(isset($user->data) && count($user->data) > 0)
		{
			if(isset($data->photo) && FileUtil::isBase64($data->photo))
			{
				$url = FileUtil::saveFile($data->photo, "$id.jpg", CAT_PARENT);
			
				if(!$url)
				{
					self::$response->hasError = true;
					self::$response->message = 'Please use a valid profile image';
					return self::$response;
				}
				
				$data->photo = $url;
			}
			else if(isset($data->photo) && !FileUtil::validateUrl($data->photo))
			{
				self::$response->hasError = true;
				self::$response->message = 'Please enter a valid profile url';
				return self::$response;
			}

			if(isset($data->birthday))
			{
				$date = DateTime::createFromFormat('F d, Y', $data->birthday);
				$date = $date->format('Y-m-d');
				$data->birthday = $date;
			}

			if(isset($data->gender))
				$data->gender = $data->gender == 'Male' ? 1 : 0;

			if(isset($data->child) && count($data->child) > 0)
				self::updateChild($data->child, ((object) array('parent' => $id)));

			if(isset($data->removedChild) && count($data->removedChild) > 0)
				self::updateChild($data->removedChild, ((object) array('parent' => 0)));

			$query = "UPDATE " . self::$table . " SET ";
			
			foreach($data as $key => $val)
				if($key == 'id' || $key == 'hasError' || $key == 'child' || $key == 'removedChild' || $key == 'message' || $key == 'created_at')
					continue;
				else
					$query .= "$key='$val', ";
			
			$query = substr($query, 0, strlen($query) - 2);
			$query .= " WHERE id=('$id')";
			$res = self::$conn->query($query);

			self::$response->hasError = !$res;
			self::$response->message = $res ? 'Parent has been updated successfully' : self::$conn->error;

			if(isset(self::$response->data))
				unset(self::$response->data);
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Parent does not exist in the database';
		}

		return self::$response;
	}
	
	public static function delete(int $id)
	{
		$user = self::get($id);
		
		if(isset($user->data) && count($user->data) > 0)
		{
			$query = "UPDATE " . self::$table . " SET deleted='1' WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->hasError = !$res;
			self::$response->message = $res ? 'Parent has been deleted successfully' : self::$conn->error;
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Parent does not exist in the database';
		}

		return self::$response;
	}

	public static function permanentDelete(int $id)
	{
		$queryOff = "SET FOREIGN_KEY_CHECKS=0";
		$queryOn = "SET FOREIGN_KEY_CHECKS=1";
		$user = self::get($id);

		if(isset($user->data) && count($user->data) > 0)
		{
			$query = "DELETE FROM " . self::$table . " WHERE id='$id'";
			self::$conn->query($queryOff);
			$res = self::$conn->query($query);
			self::$conn->query($queryOn);

			self::$response->hasError = !$res;
			self::$response->message = $res ? 'Parent has been deleted successfully' : self::$conn->error;

			FileUtil::deleteFile(DIR_UPLOADS . "/photos/parents/$id.jpg");
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Parent does not exist in the database';
		}

		return self::$response;
	}
	
	public static function add(object $data)
	{
		$query = "SELECT * FROM " .self::$table . " WHERE name='" . $data->name . "'";
		$res = self::$conn->query($query);
		
		if($res->num_rows == 0)
		{
			$query = "INSERT INTO " . self::$table . "(";

			$date = date('Y-m-d h:i:s', time());
			$data->created_at = $date;

			$data->gender = $data->gender == "Male" ? 1 : 0;

			$date = DateTime::createFromFormat('F d, Y', $data->birthday);
			$date = $date->format('Y-m-d');
			$data->birthday = $date;
			
			foreach($data as $key => $val)
				if($key != 'child' && $key != 'photo')
					$query .= "$key, ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ") VALUES(";
			
			foreach($data as $key => $val)
				if($key != 'child' && $key != 'photo')
					$query .= "'$val', ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ")";
			
			$res = self::$conn->query($query);
			
			if($res)
			{
				$id = self::$conn->insert_id;
				$res = FileUtil::saveFile($data->photo, "$id.jpg", CAT_PARENT);
				
				if(!$res)
				{
					self::$response->hasError = !$res;
					self::$response->message = 'Please use a valid profile image';
					return self::$response;
				}

				$res = self::update($id, ((object) ['photo' => $res]));

				if($res)
					if(isset($data->child) && count($data->child) > 0)
						return self::updateChild($data->child, (object) ['parent' => $id]);
					else
					{
						self::$response->hasError = !$res;
						self::$response->message = 'Successfully added parent';
					}
				else
				{
					self::$response->hasError = !$res;
					self::$response->message = 'Failed to set profile picture';
				}
			}
			else
			{
				self::$response->hasError = !$res;
				self::$response->message = self::$conn->error;
			}
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Parent already exist in the database';
		}
		
		return self::$response;
	}
	
	public static function restore(int $id)
	{
		$user = self::get($id);
		
		if(isset($user->data) && count($user->data) > 0)
		{
			$query = "UPDATE " . self::$table . " SET deleted='0' WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->hasError = !$res;
			self::$response->message = $res ? 'Parent has been restored successfully' : self::$conn->error;
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Parent does not exist in the database';
		}

		return self::$response;
	}

	public static function updateChild(array $childIds, object $data)
	{
		$done = false;

		for($i = 0; $i < count($childIds); $i++)
			$done = Student::update($childIds[$i], $data);
		
		if($done)
		{
			self::$response->hasError = !$done;
			self::$response->message = 'Successfully added parent and linked to his/her children';
		}
		else
		{
			self::$response->hasError = false;
			self::$response->message = 'Successfully added parent but failed to link to some of his/her children';
		}

		return self::$response;
	}
}

?>