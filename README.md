
# Bienvenido al readme del API REST de suma mas porcentaje de Carlos Moreno!

En este archivo readme veremos las características del API, su instalación con docker, su uso con postman o swagger-ui y algunas otras caracteristicas.

# Caracteristicas

Esta API REST esta d esarollada con el framework Spring Boot utilizando java 17 y posee las siguientes funcionalidades:

Contiene un servicio llamado por api-rest que recibe 2 números, los suma, y le aplique una suma de un porcentaje que debe ser adquiere de un servicio externo (por ejemplo, si el servicio recibe 5 y 5 como valores, y el porcentaje devuelto por el servicio externo es 10, entonces (5 + 5) + 10% = 11). 

Esta API tiene las siguientes funcionalidades:

1. Ya desplegado se puede consultar el siguiente endpoint **/api/sum**
2. El servicio externo es un mock, y devuelve el % sumado.
3. Dado que ese % varía poco, podemos considerar que el valor que devuelve ese servicio no va cambiar por 30 minutos.
4. Se puede simular que el servicio externo falle con el endpoint **/api/simulate** y se ejecuta de la siguiente manera para simular que falla:		![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/SimuladorServicio.PNG)
		4.Para reanudar el servicio se ejecuta dela siguiente manera:	
		![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/SimuladorServicioActivar.PNG)
5. Si el servicio falla, se puede reintentar consultar hasta 3 veces, luego de esto se mostrará el siguiente error.
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Respuesta%20fallo%20del%20servicio.PNG)

6.El Historial de todos los llamados a todos los endpoint se guarda en una BD PostgreSQL junto con la respuesta en caso de haber sido exitoso o fallido. 
6. El historial de los llamados a los endpoint se pueden consultar en este endpoint **/api/history/** este respondera con un Json, con data paginada con la opcion de la pagina y la cantidad de registros por pagina asi:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/HistoricoLlamados.PNG)
7. El guardado del historial de llamadas no suma tiempo al servicio invocado.
8. La API soporta como máximo 3 rpm (request / minuto), en caso de superar ese umbral se retorna un error con el código http "429  Too Many Requests" y el mensaje "Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en xx segundos." La API espera 1 minuto para poder reanudar las consultas y muestra con un conteo regresivo cada vez que se consulta el tiempo restante para poder usar.
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/LLamadas%20por%20minuto.PNG)

9. El api incluye tests unitarios en la clase **ApiSumaApplicationTests**, se pueden ejecutar con el comando de generación del jar:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Unit%20Test%2001.PNG)
Aquí el resultado:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Unit%20Test%2001%20Result.PNG)
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Unit%20Test.PNG)

10. Esta API puede ser desplegada en un docker container. Este docker está en un dockerhub público. La base de datos se puede correr en un contenedor docker. A continuacion archivo docker compose:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Docker%20Compose.PNG)

4-El proyecto contiene Swagger con las librerias **springdoc-openapi-starter-webmvc-ui** y  **springdoc-openapi-starter-webmvc-api** para probar la API y se puede probar ejecutando localmente con **mvn spring-boot:run** o desde docker. La ulr es **http://localhost:8085/swagger-ui/index.html#/** y se muestra así:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Swagger.PNG)
11. En la carpeat de la raiz del proyecto también podemos encontrar un archivo  **postman_collection.json** que podemos abrir con Postman dekstop para probar el api. Revisar si esta despelgado en docker o local por que puede varias el puerto del endpoint.
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Postman.PNG) 
5-El código esta disponible en un repositorio público de gitHub en el link *https://github.com/CharlieJEE/apiSuma**


## Instalación
Empecemos bajando el código del repositorio de Git desde la siguiente url https://github.com/CharlieJEE/apiSuma luego copiamos la ulr del repositorio para clonarlo localmente:
**https://github.com/CharlieJEE/apiSuma.git**

![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion01.PNG)
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion02.png)

luego con gitbash o con una aplicacion de git clonamos el repositorio
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion03.PNG)

![com oabrir powershell](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Captura.PNG)
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion04.PNG)
Verificamos la descarga del repositorio:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion05.PNG)

Iniciamos docker y abrimos un powershell
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/Captura.PNG)
 En power shell ejecutamos **docker ps** y **docker images** para revisar que contenedores e imágenes tenemos desplegadas:
 ![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion06.PNG)

Nos ubicamos en el directorio raiz del proyecto clonado y luego ejecutamos **docker-compose up** (Si no esta generado el jar podemos generarlo desde un IDE o un shell con el comando **mvn clean install**)

![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion07.PNG)
Verificamos que se ejecuta correctamente el comando sin errores
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion08.PNG)
al finalizar consultamos **http://localhost:8082/swagger-ui/index.html** para verificar si nuestra aplicación ha sido desplegada correctamente:
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion09.PNG)
Tambien revisamos en postman su funcionamiento
![enter image description here](https://raw.githubusercontent.com/CharlieJEE/apiSuma/main/img_readme/install/Instalacion10.PNG)

tambien se puede bajar la imagen desde dockerhub.com con el siguiente comando **docker pull karlos23/repo-publico**

## Agradecimiento

Gracias por usar el APIREST creada por Carlos Arturo Moreno Junio de 2023.
