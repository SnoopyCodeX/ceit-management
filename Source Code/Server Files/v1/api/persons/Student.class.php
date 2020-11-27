<?php

class Student {
	private static $response;
	private static $table = 'students';
	private static $conn;
	
	public function __construct($db)
	{
		self::$conn = $db;
		self::$response = (object) array();
	}
	
	public static function init($db)
	{
		return new Student($db);
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
					$query = "SELECT * FROM classes WHERE id='" . $row['section'] . "'";
					$query = self::$conn->query($query);

					if($query && $query->num_rows > 0)
					{
						$class = $query->fetch_assoc();
						$section = $class['name'];
						$row['section'] = $section;
					}
					else
						$row['section'] = "No class specified";

					$row['gender'] = $row['gender'] == 0 ? 'Female' : 'Male';

					$birthday = $row['birthday'];
					$date = date_create($birthday, timezone_open('Asia/Manila'));
					$date = date_format($date, 'M d, Y');
					$row['birthday'] = $date;

					array_push($data, $row);
				}
				
				self::$response->data = $data;
				self::$response->hasError = false;
			}
			else
			{
				self::$response->message = "No data was found in the database";
				self::$response->hasError = true;
			}
		}
		else
		{
			self::$response->message = self::$conn->error;
			self::$response->hasError = true;
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
				$query = "SELECT * FROM classes WHERE id='" . $row['section'] . "'";
				$query = self::$conn->query($query);

				if($query && $query->num_rows > 0)
				{
					$class = $query->fetch_assoc();
					$name = $class['name'];
					$row['section'] = $name;
				}
				else
					$row['section'] = "No class specified";


				$row['gender'] = $row['gender'] == 0 ? 'Female' : 'Male';

				array_push($data, $row);
			}

			self::$response->data = $data;
			self::$response->hasError = false;
		}
		else
		{
			self::$response->message = $res ? "No data was found in the database" : self::$conn->error;
			self::$response->hasError = false;
		}

		return self::$response;
	}
	
	public static function update(int $id, object $data)
	{
		$user = self::get($id);
		
		if(count($user->data) > 0)
		{
			if(count(Section::get()->data) <= 0)
			{
				self::$response->hasError = true;
				self::$response->message = 'Please create a section first before updating a student';
				return self::$response;
			}

			if(isset($data->photo) && FileUtil::isBase64($data->photo))
			{
				$url = FileUtil::saveFile($data->photo, "$id.jpg", CAT_STUDENT);
			
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

			if(isset($data->gender))
				$data->gender = $data->gender == 'Male' ? 1 : 0;

			if(isset($data->birthday))
			{
				$date = DateTime::createFromFormat('F d, Y', $data->birthday);
				$date = $date->format('Y-m-d');
				$data->birthday = $date;
			}

			if(isset($data->section))
			{
				$query = "SELECT * FROM classes WHERE name='" . $data->section . "'";
				$res = self::$conn->query($query);

				if($res && $res->num_rows > 0)
				{
					$row = (object) $res->fetch_assoc();
					$data->section = $row->id;
				}
				else
					$data->section = -1;
			}
			
			$query = "UPDATE " . self::$table . " SET ";
			
			foreach($data as $key => $val)
				if($key == 'id' || $key == 'hasError' || $key == 'message' || $key == 'created_at')
					continue;
				else
					$query .= "$key='$val', ";
			
			$query = substr($query, 0, strlen($query) - 2);
			$query .= " WHERE id=('$id')";

			$queryOn = "SET FOREIGN_KEY_CHECKS=1";
			$queryOff = "SET FOREIGN_KEY_CHECKS=0";

			if(isset($data->parent) && $data->parent <= 0)
				self::$conn->query($queryOff);
			
			$res = self::$conn->query($query);

			if(isset($data->parent) && $data->parent <= 0)
				self::$conn->query($queryOff);
			
			self::$response->hasError = !$res;
			self::$response->message = $res ? "Student has been updated successfully" : self::$conn->error; 
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = 'Student does not exist in the database';
		}

		return self::$response;
	}
	
	public static function delete(int $id)
	{
		$user = self::get($id);
		
		if(count($user->data) > 0)
		{
			$query = "UPDATE " . self::$table . " SET deleted='1' WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->hasError = !$res;
			self::$response->message = $res ? "Student has been deleted successfully" : self::$conn->error; 
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = "Student does not exist in the database"; 
		}
		
		return self::$response;
	}

	public static function permanentDelete($id)
	{
		$user = self::get($id);

		if(count($user->data) > 0)
		{
			$query = "DELETE FROM " . self::$table . " WHERE id='$id'";
			$res = self::$conn->query($query);
			
			self::$response->hasError = !$res;
			self::$response->message = $res ? "Student has been deleted successfully" : self::$conn->error;
			
			FileUtil::deleteFile(DIR_UPLOADS . "/photos/students/$id.jpg");
		}
		else
		{
			self::$response->hasError = true;
			self::$response->message = "Student does not exist in the database"; 
		}

		return self::$response;
	}
	
	public static function add(object $data)
	{
		if(count(Section::get()->data) <= 0)
		{
			self::$response->hasError = true;
			self::$response->message = 'Please create a section first before adding in a student';
			return self::$response;
		}

		$query = "SELECT * FROM " .self::$table . " WHERE name='" . $data->name . "'";
		$res = self::$conn->query($query);
		
		if($res->num_rows == 0)
		{
			$date = date('Y-m-d h:i:s', time());
			$data->created_at = $date;

			$data->gender = $data->gender == "Male" ? 1 : 0;

			$date = DateTime::createFromFormat('F d, Y', $data->birthday);
			$date = $date->format('Y-m-d');
			$data->birthday = $date;

			$query = "SELECT * FROM classes WHERE name='" . $data->section . "'";
			$res = self::$conn->query($query);

			if($res && $res->num_rows > 0)
			{
				$row = (object) $res->fetch_assoc();
				$data->section = $row->id;
			}
			else
			{
				self::$response->hasError = true;
				self::$response->message = $res ? 'Please create a section first before adding in a student' : self::$conn->error;
				return self::$response;
			}

			$data->parent = -1;
			
			$queryOff = "SET FOREIGN_KEY_CHECKS=0";
			$queryOn = "SET FOREIGN_KEY_CHECKS=1";
			$query = "INSERT INTO " . self::$table . "(";

			self::$conn->query($queryOff);

			foreach($data as $key => $val)
				if($key != 'photo')
					$query .= "$key, ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ") VALUES(";
			
			foreach($data as $key => $val)
				if($key != 'photo')
					$query .= "'$val', ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ");";
			
			$res = self::$conn->query($query);
			
			if($res)
			{
				$id = self::$conn->insert_id;
				$res = FileUtil::saveFile($data->photo, "$id.jpg", CAT_STUDENT);
				self::$conn->query($queryOn);
				
				if(!$res)
				{
					self::$response->message = "Please use a valid profile image";
					self::$response->hasError = !$res;
					return self::$response;
				}
				
				$res = self::update($id, ((object) array('photo' => $res)));
				self::$response->message = "Student has been added successfully";
				self::$response->hasError = !$res;
			}
			else
			{
				self::$response->message = self::$conn->error;
				self::$response->hasError = !$res;
			}
		}
		else
		{
			self::$response->message = "Student already exist in the database";
			self::$response->hasError = true;
		}

		return self::$response;
	}
	
	public static function restore(int $id)
	{
		$query = "SELECT * FROM " . self::$table . " WHERE id='$id'";
		$user = self::$conn->query($query);
		
		if($user->num_rows > 0)
		{
			$query = "UPDATE " . self::$table . " SET deleted='0' WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->message = $res ? "Student has been added successfully" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = $res ? "Student does not exist in the database" : self::$conn->error;
			self::$response->hasError = true;
		}

		return self::$response;
	}
}

?>