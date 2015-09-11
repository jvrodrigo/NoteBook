<?php

	// Definición de métodos en nuestro servicio web -------------------------------------------


	$server->register(
		'guardarPost',
		array('titulo'=>'xsd:string','texto'=>'xsd:string'),
		array('return'=>'xsd:void'),
		SOAP_SERVER_NAMESPACE,                	 // Nombre del workspace
		SOAP_SERVER_NAMESPACE.'#guardarPost',        // Acción soap
		'rpc',                                	 // Estilo
		'encoded',                            	 // Uso
		'Guarda un Post en el servidor' 		 // Documentación
	);

?>
