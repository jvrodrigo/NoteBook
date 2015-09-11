<?php

function guardarPost($title=null,$text=null){
	//Conexion con la base
	mysql_connect('mysql.hostinger.es','u698943981_nbook','456rtyfghvbn');
//selección de la base de datos con la que vamos a trabajar
	mysql_select_db("u698943981_nbook");
//Ejecucion de la sentencia SQL
	mysql_query("insert into POSTS (title,text) values ('$title','$text')");

}
?>
