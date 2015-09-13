<?php
echo '<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
	<title>
		Plataformas Software Moviles: Servicio SOAP para Android
	</title>
</head>
<body>';?>
<?php
echo '<h1>Bienvenido al servicio SOAP para Android: Plataformas Software Moviles</h1><span>Por: Debora Maura y Jose Vicente Rodrigo</span>';
echo '<p><a href="NoteBook.apk">Descargate el apk de la aplicación</a></p>';
$conexion = mysql_connect('mysql.default_host','user','password');
$ok = mysql_select_db('data_base_name',$conexion);
$consulta = 'SELECT title,text,created FROM POSTS ORDER BY created DESC;';
$response = mysql_query($consulta);

while ($resultado = mysql_fetch_assoc($response)){
	echo '<h3>Título: '.$resultado['title'].'</h3>';
	echo '<span>Creado: '.$resultado['created'].'</span>';
	echo '<p>'.$resultado['text'].'</p>';
}
echo '</body>
</html>';

?>
