<?php
require_once('api/autoload.php');

$response = array(
	"hasError" => false,
	"message" => ""
);

// USER ROUTES
Route::add('/user/login', function() {
	global $response;
	
	$data = file_get_contents("php://input");
	$data = json_decode($data);

	$request = explode('.', $data->request);

	if(count($request) < 2)
	{
		$response['message'] = 'Invalid request was sent';
		$response['hasError'] = true;
		echo json_encode($response);
		return;
	}

	$file = DIR_ETC . '/passwd.txt';
	chmod($file, 0700);
	$json = file_get_contents($file);
	$passwd = json_decode($json);
	$user = base64_decode($request[0]);
	$pass = base64_decode($request[1]);
	
	if($user == $passwd->user)
	{
		if($pass == $passwd->pass)
		{
			$response['hasError'] = false;
		}
		else
		{
			$response['message'] = 'Invalid password';
			$response['hasError'] = true;
		}
	}
	else
	{
		$response['message'] = 'Username does not exist';
		$response['hasError'] = true;
	}
	
	chmod($file, 0000);
	echo json_encode($response);
}, 'POST');

Route::add('/user/update', function() {
	global $response;

	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	FileUtil::saveFile(base64_encode(json_encode($data)), 'test.txt');

	$request = explode('.', $data->request);

	if(count($request) < 4)
	{
		$response['message'] = 'Invalid request was sent';
		$response['hasError'] = true;
		echo json_encode($response);
		return;
	}

	$file = DIR_ETC . "/passwd.txt";
	chmod($file, 0700);
	$passwd = json_decode(file_get_contents($file));
	chmod($file, 0000);
	$olduser = base64_decode($request[0]);
	$oldpass = base64_decode($request[1]);
	$newuser = base64_decode($request[2]);
	$newpass = base64_decode($request[3]);


	if($olduser == $passwd->user)
	{
		if($oldpass == $passwd->pass)
		{
			$txt = json_encode(['user' => $newuser, 'pass' => $newpass]);
			$file = DIR_ETC . '/passwd.txt';

			chmod($file, 0700);
			file_put_contents(DIR_ETC . '/passwd.txt', $txt);
			chmod($file, 0000);

			$response['hasError'] = false;
			$response['message'] = 'Credentials has been updated successfully!';
		}
		else
		{
			$response['message'] = 'Invalid password';
			$response['hasError'] = true;
		}
	}
	else
	{
		$response['message'] = 'Invalid username';
		$response['hasError'] = true;
	}

	echo json_encode($response);
}, 'POST');

Route::add('/user/database/reset', function() {
	Database::$conn->query('SET FOREIGN_KEY_CHECKS=0');
	$res1 = Database::$conn->query('TRUNCATE TABLE `parents`');
	$res2 = Database::$conn->query('TRUNCATE TABLE `teachers`');
	$res3 = Database::$conn->query('TRUNCATE TABLE `classes`');
	$res4 = Database::$conn->query('TRUNCATE TABLE `students`');
	Database::$conn->query('SET FOREIGN_KEY_CHECKS=1');
	$response = ['message' => '', 'hasError' => false];

	if($res1 && $res2 && $res3 && $res4)
		echo json_encode($response);
	else
	{
		$response['message'] = 'The server failed to reset the database';
		$response['hasError'] = true;
		echo json_encode($response);
	}
}, 'POST');

// TEACHER ROUTES
Route::add('/teachers\/*', function() {
	$response = Teacher::get();
	echo json_encode($response);
});

Route::add('/teachers/removed', function() {
	$response = Teacher::getAllRemoved();
	echo json_encode($response);
});

Route::add('/teachers/([0-9]+)$', function($id) {
	$response = Teacher::get($id);
	echo json_encode($response);
});

Route::add('/teachers/([0-9]+)/update', function($id) {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != "photo")
			$data->$key = Database::$conn->real_escape_string($val);

	$response = Teacher::update($id, $data);
	echo json_encode($response);
}, 'POST');

Route::add('/teachers/([0-9]+)/delete', function($id) {
	$response = Teacher::delete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/teachers/([0-9]+)/delete/permanent', function($id) {
	$response = Teacher::permanentDelete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/teachers/([0-9]+)/restore', function($id) {
	$response = Teacher::restore($id);
	echo json_encode($response);
}, 'POST');

Route::add('/teachers/new', function() {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != "photo")
			$data->$key = Database::$conn->real_escape_string($val);
	
	$response = Teacher::add($data);
	echo json_encode($response);
}, 'POST');

Route::add('/teachers/([0-9]+)/profile', function($id) {
	$data = Teacher::get($id);
	
	if(count($data->data) > 0)
		FileUtil::getImage(DIR_UPLOADS . "/photos/teachers/$id.jpg");
	
});

// STUDENT ROUTES
Route::add('/students\/*', function() {
	$response = Student::get();
	echo json_encode($response);
});

Route::add('/students/removed', function() {
	$response = Student::getAllRemoved();
	echo json_encode($response);
});

Route::add('/students/([0-9]+)$', function($id) {
	$response = Student::get($id);
	echo json_encode($response);
});

Route::add('/students/([0-9]+)/update', function($id) {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != "photo")
			$data->$key = Database::$conn->real_escape_string($val);
	
	$response = Student::update($id, $data);
	echo json_encode($response);
	
}, 'POST');

Route::add('/students/([0-9]+)/delete', function($id) {
	$response = Student::delete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/students/([0-9]+)/delete/permanent', function($id) {
	$response = Student::permanentDelete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/students/([0-9]+)/restore', function($id) {
	$response = Student::restore($id);
	echo json_encode($response);
}, 'POST');

Route::add('/students/new', function() {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != "photo")
			$data->$key = Database::$conn->real_escape_string($val);
	
	$response = Student::add($data);
	echo json_encode($response);
}, 'POST');

Route::add('/students/([0-9]+)/profile', function($id) {
	$data = Student::get($id);
	
	if(count($data->data) > 0)
		FileUtil::getImage(DIR_UPLOADS . "/photos/students/$id.jpg");
});

// PARENT ROUTES
Route::add('/parents\/*', function() {
	$response = _Parent::get();
	echo json_encode($response);
});

Route::add('/parents/removed', function() {
	$response = _Parent::getAllRemoved();
	echo json_encode($response);
});

Route::add('/parents/([0-9]+)$', function($id) {
	$response = _Parent::get($id);
	echo json_encode($response);
});

Route::add('/parents/([0-9]+)/update', function($id) {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != "photo")
			$data->$key = Database::$conn->real_escape_string($val);

	FileUtil::saveFile(base64_encode(json_encode($data)), 'parent_update_test.txt');
	
	$response = _Parent::update($id, $data);
	echo json_encode($response);
}, 'POST');

Route::add('/parents/([0-9]+)/delete', function($id) {
	$response = _Parent::delete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/parents/([0-9]+)/delete/permanent', function($id) {
	$response = _Parent::permanentDelete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/parents/([0-9]+)/restore', function($id) {
	$response = _Parent::restore($id);
	echo json_encode($response);
}, 'POST');

Route::add('/parents/new', function() {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string && $key != 'photo')
			$data->$key = Database::$conn->real_escape_string($val);
	
	FileUtil::saveFile(base64_encode(json_encode((array) $data)), 'data_dump.txt');
	$response = _Parent::add($data);
	echo json_encode($response);
}, 'POST');

Route::add('/parents/([0-9]+)/profile', function($id) {
	$data = _Parent::get($id);
	
	if(count($data->data) > 0)
		FileUtil::getImage(DIR_UPLOADS . "/photos/parents/$id.jpg");
});

// CLASS ROUTES
Route::add('/classes\/*', function() {
	$response = Section::get();
	echo json_encode($response);
});

Route::add('/classes/removed', function() {
	$response = Section::getAllRemoved();
	echo json_encode($response);
});

Route::add('/classes/([0-9]+)$', function($id) {
	$response = Section::get($id);
	echo json_encode($response);
});

Route::add('/classes/([0-9]+)/remove/([0-9]+)$', function($classId, $studentId) {
	$response = Section::removeStudent($classId, $studentId);
	echo json_encode($response);
});

Route::add('/classes/([0-9]+)/update', function($id) {
	$data = file_get_contents("php://input");
	$data = json_decode($data);

	foreach($data as $key => $val)
		if($val instanceof string)
			$data->$key = Database::$conn->real_escape_string($val);
	
	$response = Section::update($id, $data);
	echo json_encode($response);
	
}, 'POST');

Route::add('/classes/([0-9]+)/delete', function($id) {
	$response = Section::delete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/classes/([0-9]+)/delete/permanent', function($id) {
	$response = Section::permanentDelete($id);
	echo json_encode($response);
}, 'POST');

Route::add('/classes/([0-9]+)/restore', function($id) {
	$response = Section::restore($id);
	echo json_encode($response);
}, 'POST');

Route::add('/classes/new', function() {
	$data = file_get_contents("php://input");
	$data = json_decode($data);
	
	foreach($data as $key => $val)
		if($val instanceof string)
			$data->$key = Database::$conn->real_escape_string($val);
	
	$response = Section::add($data);
	echo json_encode($response);
}, 'POST');

// MISCELLANEOUS ROUTES
Route::add('/files/misc/([a-zA-Z0-9_\-\.]+)$', function($fileId) {
	header("Content-Type: " . mime_content_type(DIR_UPLOADS . "/photos/misc/$fileId"));
	echo FileUtil::getFileData(DIR_UPLOADS . "/photos/misc/$fileId");
});

Route::run('/v1');
?>