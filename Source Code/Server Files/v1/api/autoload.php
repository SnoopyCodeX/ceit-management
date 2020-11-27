<?php
require_once('config/Config.php');
require_once(DIR_CONFIG . '/Database.class.php');
require_once(DIR_CORE . '/Router.class.php');
require_once(DIR_CORE . '/FileUtil.class.php');
require_once(DIR_PERSONS . '/Student.class.php');
require_once(DIR_PERSONS . '/Section.class.php');
require_once(DIR_PERSONS . '/Teacher.class.php');
require_once(DIR_PERSONS . '/Parent.class.php');


Database::init();
Teacher::init(Database::$conn);
Student::init(Database::$conn);
_Parent::init(Database::$conn);
Section::init(Database::$conn);
?>