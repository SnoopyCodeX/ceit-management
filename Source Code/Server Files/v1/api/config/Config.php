<?php

$configs = array(
	"DIRECTORIES" => array(
		"DIR_UPLOADS" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/uploads",
		"DIR_CORE" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/core",
		"DIR_PERSONS" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/persons",
		"DIR_CONFIG" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/config",
		"DIR_ETC" => $_SERVER['DOCUMENT_ROOT'] . '/v1/etc'
	),
	"FILE CATEGORIES" => array(
		"CAT_TEACHER" => "teacher",
		"CAT_STUDENT" => "student",
		"CAT_PARENT" => "parent",
		"CAT_MISC" => "miscellaneous"
	)
);

foreach($configs as $config)
	foreach($config as $key => $val)
		define($key, $val);

?>