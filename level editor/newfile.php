<?php
	$my_file = "data/".$_POST["filename"].".json";
	$handle = fopen($my_file, 'a') or die('Cannot open file:  '.$my_file);
	$data = "";
	fwrite($handle, $data);
	fclose($handle);
	$handle = fopen("data/info/filenames.txt", 'a');
	fwrite($handle, $_POST["filename"].",");
	fclose($handle);
?>