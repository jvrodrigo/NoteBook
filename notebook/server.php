<?php
	ini_set( "display_errors", "on" );

	// Incluye el archivo de configuración de nuestro servidor web
	require_once('./common/config.php');

	// Incluye el archivo de funciones
	require_once('./common/functions.php');
	
	// Incluye la librería NuSOAP
	require_once('./lib/nusoap.php');

	// Configura el WSDL
	$server = new soap_server();
	$server->debug_flag = SOAP_SERVER_DEBUG_MODE;
	$server->configureWSDL(SOAP_SERVER_NAME, SOAP_SERVER_NAMESPACE);
	$server->wsdl->schemaTargetNamespace = SOAP_SERVER_NAMESPACE;
	$server->soap_defencoding = SOAP_SERVER_ENCODING; 
	
	// Definición de tipos y metodos en nuestro servicio web
	require_once './common/definitions.php';

	// Código para invocar el servicio.
	$HTTP_RAW_POST_DATA = isset($GLOBALS['HTTP_RAW_POST_DATA']) ? $GLOBALS['HTTP_RAW_POST_DATA'] : '';
			
	$server->service($HTTP_RAW_POST_DATA);
?>
