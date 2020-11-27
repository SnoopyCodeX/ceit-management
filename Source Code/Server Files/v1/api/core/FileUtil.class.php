<?php

class FileUtil {
	public function __construct()
	{}
	
	public static function saveFile(string $data, string $name, string $category = CAT_MISC)
	{
		if(!self::isBase64($data))
			return false;
		
		$data = base64_decode($data);
		$url = "https://www.ceit-management.ml/v1/";
		$dir = "";
		
		switch($category)
		{
			case CAT_TEACHER:
				$dir = DIR_UPLOADS . "/photos/teachers/$name";
				$name = basename($dir, '.' . explode('.', $dir)[1]);
				$url .= "teachers/$name/profile";
			break;
			
			case CAT_STUDENT:
				$dir = DIR_UPLOADS . "/photos/students/$name";
				$name = basename($dir, '.' . explode('.', $dir)[1]);
				$url .= "students/$name/profile";
			break;
			
			case CAT_PARENT:
				$dir = DIR_UPLOADS . "/photos/parents/$name";
				$name = basename($dir, '.' . explode('.', $dir)[1]);
				$url .= "parents/$name/profile";
			break;
			
			case CAT_MISC:
				$dir = DIR_UPLOADS . "/photos/misc/$name";
				$url .= "misc/$name";
			break;
		}
		
		$res = file_put_contents($dir, $data);
		
		if(!$res)
			return false;
		
		
		return $url;
	}
	
	public static function getImage($dir)
	{
		if(!file_exists($dir))
			return false;

		$img = file_get_contents($dir);
		header('Content-Type: ' . mime_content_type($dir));
		echo $img;
	}
	
	public static function getFileData($dir)
	{
		if(!file_exists($dir))
			return null;

		return (file_get_contents($dir));
	}

	public static function deleteFile($dir)
	{
		if(!file_exists($dir))
			return false;

		return unlink($dir);
	}
	
	public static function isBase64(string $str)
	{
		return (base64_decode($str, true) !== false);
	}

	public static function validateUrl($url) 
	{
		$path = parse_url($url, PHP_URL_PATH);
		$encoded_path = array_map('urlencode', explode('/', $path));
		$url = str_replace($path, implode('/', $encoded_path), $url);
	
		return filter_var($url, FILTER_VALIDATE_URL) ? true : false;
	}
}

?>