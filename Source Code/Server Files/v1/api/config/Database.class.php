<?php

class Database {
    private $host = "localhost";
    private $db_name = "id15156498_db_ceit";
    private $username = "id15156498_root";
    private $password = "toor@CMdb2020";
    public static $conn;
	
	private function __construct()
	{
		self::$conn = new mysqli($this->host, $this->username, $this->password, $this->db_name);
	}
	
	public static function init()
	{
		return new Database();
	}
}

?>