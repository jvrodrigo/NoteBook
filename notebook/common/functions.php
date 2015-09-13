<?php

function guardarPost($title=null,$text=null){
	//Conexion con la base
	mysql_connect('mysql.default_host','user_name','password');
//selección de la base de datos con la que vamos a trabajar
	mysql_select_db("data_base_name");
//Ejecucion de la sentencia SQL
	mysql_query("insert into POSTS (title,text) values ('$title','$text')");

}
?>
