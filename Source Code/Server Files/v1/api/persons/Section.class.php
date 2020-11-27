<?php

class Section {
	private static $response;
	private static $table = 'classes';
	private static $conn;
	
	public function __construct($db)
	{
		self::$conn = $db;
		self::$response = (object) self::$response;
	}
	
	public static function init($db)
	{
		return new Section($db);
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
					$query = "SELECT * FROM teachers WHERE id='" . $row['teacher'] . "'";
					$query = self::$conn->query($query);

					if($query && $query->num_rows > 0)
					{
						$teacher = $query->fetch_assoc();
						$name = $teacher['name'];
						$row['teacher'] = $name;
					}
					else
						$row['teacher'] = "No teacher assigned";

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
				$query = "SELECT * FROM teachers WHERE id='" . $row['teacher'] . "'";
				$query = self::$conn->query($query);

				if($query && $query->num_rows > 0)
				{
					$teacher = $query->fetch_assoc();
					$name = $teacher['name'];
					$row['teacher'] = $name;
				}
				else
					$row['teacher'] = "No teacher assigned";

				array_push($data, $row);
			}

			self::$response->data = $data;
			self::$response->hasError = false;
		}
		else
		{
			self::$response->message = $res ? "No data was found in the database" : self::$conn->error;
			self::$response->hasError = !$res;
		}

		return self::$response;
	}
	
	public static function update(int $id, object $data)
	{
		$user = self::get($id);
		
		if(count($user->data) > 0)
		{
			$query = "SELECT * FROM teachers WHERE name='" . $data->teacher . "'";
			$res = self::$conn->query($query);

			if($res->num_rows > 0)
				$data->teacher = ($res->fetch_assoc())['id'];
			else
			{
				self::$response->message = "Please add a teacher first before creating a class";
				self::$response->hasError = true;
				return self::$response;
			}

			$query = "UPDATE " . self::$table . " SET ";
			
			foreach($data as $key => $val)
				if($key == 'id' || $key == 'hasError' || $key == 'message' || $key == 'created_at')
					continue;
				else
					$query .= "$key='$val', ";
			
			$query = substr($query, 0, strlen($query) - 2);
			$query .= " WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->message = $res ? "Class has been updated successfully" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = "No data was found in the database";
			self::$response->hasError = true;
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
			
			self::$response->message = $res ? "Class has been deleted successfully" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = "No data was found in the database";
			self::$response->hasError = true;
		}

		return self::$response;
	}

	public static function permanentDelete(int $id)
	{
		$user = self::get($id);

		if(count($user->data) > 0)
		{
			$query = "DELETE FROM " . self::$table . " WHERE id='$id'";
			$res = self::$conn->query($query);

			self::$response->message = $res ? "Class has been successfully deleted" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = "No data was found in the database";
			self::$response->hasError = true;
		}

		return self::$response;
	}
	
	public static function add(object $data)
	{
		$query = "SELECT * FROM " .self::$table . " WHERE name='" . $data->name . "'";
		$res = self::$conn->query($query);
		
		if($res->num_rows == 0)
		{
			$query = "SELECT * FROM teachers WHERE name='" . $data->teacher . "'";
			$res = self::$conn->query($query);

			if($res->num_rows > 0)
				$data->teacher = ($res->fetch_assoc())['id'];
			else
			{
				self::$response->message = "Please add a teacher first before creating a class";
				self::$response->hasError = true;
				return self::$response;
			}

			$query = "INSERT INTO " . self::$table . "(";

			$date = date('Y-m-d h:i:s', time());
			$data->created_at = $date;
			
			foreach($data as $key => $val)
				if($key != "id")
					$query .= "$key, ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ") VALUES (";
			
			foreach($data as $key => $val)
				$query .= "'$val', ";
			$query = substr($query, 0, strlen($query) - 2);
			$query .= ")";
			
			$res = self::$conn->query($query);
			self::$response->message = $res ? "Class has been successfully created" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = "Class already exist in the database";
			self::$response->hasError = true;
		}

		return self::$response;
	}
	
	public static function restore(int $id)
	{
		$user = self::get($id);
		
		if(count($user->data) > 0)
		{
			$query = "UPDATE " . self::$table . " SET deleted='0' WHERE id=('$id')";
			$res = self::$conn->query($query);
			
			self::$response->message = $res ? "Class has been successfully restored" : self::$conn->error;
			self::$response->hasError = !$res;
		}
		else
		{
			self::$response->message = "Class does not exist in the database";
			self::$response->hasError = true;
		}
		
		return self::$response;
	}
}

?>