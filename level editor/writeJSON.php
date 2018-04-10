<?php
	$my_file = "data/".$_POST["fileName"].".json";
	echo($my_file);
	$handle = fopen($my_file, 'w+') or die('Cannot open file:  '.$my_file);
	$data = '{"Levels":'.$_POST["jsonString"]."}";
	//echo("my buddy what the fucc");
	echo($data);
	fwrite($handle, $data);
	fclose($handle);
?>