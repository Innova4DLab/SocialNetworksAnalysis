package nodefriends;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
    joseluisvzg@gmail.com
*/
public class NodeFriends {
    private static HashMap<String, Amigo> amigosTable;
    private static HashSet<Link> linksSet;

    private static String email, password;//Credenciales de la cuenta semilla
    private static String uidUser, username;
    private static int maxLevel;//Nivel de exploración maximo
    private static int amigosMax;//Numero de amigos maximos a obtener

    public NodeFriends(String email, String password, int maxLevel, int amigosMax) {//Constructor
        this.email = email;
        this.password = password;
        this.maxLevel = maxLevel;
        this.amigosMax = amigosMax;
    }

    public int run() {
        //Inicializar listas
        amigosTable = new HashMap<>();
        linksSet = new HashSet<>();

        try {
            //Crear un objeto WebClient
            final WebClient webClient = new WebClient();
            //Abrir la pagina facebook dentro del Webclient
            final HtmlPage page1 = webClient.getPage("https://www.facebook.com/");
            //Seleccionar el formulario de inicio de sesión
            final HtmlForm form = (HtmlForm) page1.getElementById("login_form");
            //Seleccionar el boton de inicio de sesión
            final HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Log In").get(0);
            //Seleccionar la caja de texto "email" y ingresarle el email de la cuenta semilla
            final HtmlTextInput textField = form.getInputByName("email");
            textField.setValueAttribute(this.email);
            //Seleccionar la caja de texto "pass" y ingresarle la contraseña de la cuenta semilla
            final HtmlPasswordInput textField2 = form.getInputByName("pass");
            textField2.setValueAttribute(this.password);
            //Iniciar sesión
            final HtmlPage page2 = button.click();

            //Obtener el IDUsuario de cuenta semilla
            final HtmlPage htmlID = webClient.getPage("https://m.facebook.com/home.php");
            String ownID = parseOwnID(htmlID.getWebResponse().getContentAsString());

            if(ownID.equals("-1")){
                return -1;//IDUsuario no encontrado (credeciales incorrectas)
            }
            uidUser=ownID;

            //Obtener Username de cuenta semilla
            final HtmlPage htmlUsername = webClient.getPage("https://www.facebook.com/settings/account/");
            String ownUsername = parseUsername(htmlUsername.getWebResponse().getContentAsString());

            if(ownUsername.equals("-1")){
                return -2;//Username no encontrado (Error inesperado)
            }
            username=ownUsername;

            amigosTable.put("Me", new Amigo(ownID, "Me", 0, 1));    //Agregar a la lista la cuenta semilla

            boolean newAdditions = true;
            HashSet<Amigo> newFriends = new HashSet<>(amigosTable.values());

            while(newAdditions) {
                newAdditions = false;
                HashSet<Amigo> tmpFriends = new HashSet<>();

                for (Amigo amigo : newFriends) {
                    int levelNode = amigo.nivel;
                    if (levelNode < maxLevel) {
                        newAdditions = getFriends(webClient, amigo.uidFacebook, levelNode + 1, tmpFriends);
                    }
                }

                newFriends = tmpFriends;
                System.out.println("Tamaño de la lista amigos ahora es:" + amigosTable.size());
            }

            System.out.println("Lista de todos los amigos:");
            for(Map.Entry<String, Amigo> entry : amigosTable.entrySet()){
                System.out.println("Amigo> IDUsuario:" + entry.getKey() + ", NombreUsuario:" + entry.getValue().nombreUsuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -2;//Ocurrio un error!!!
        }
        //Almacenar amigos
        saveNodes();
        //Almacenar relaciones
        saveLinks();
        return 1;//Correct
    }


    public static String parseOwnID(String inStr){
        String pattern = "\\\\/composer\\\\/mbasic\\\\/\\?av=([0-9]{10,15})";//\\/composer\\/mbasic\\/\?av=([0-9]{10,15})

        // Crear un objeto Pattern a partir de la expresión regular
        Pattern r = Pattern.compile(pattern);

        // Aplicar la expresión regular sobre el texto
        Matcher m = r.matcher(inStr);

        //Obtener la fecha actual
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date now=new Date(System.currentTimeMillis());

        if (m.find()) {//Se encontro coincidencia
            System.out.println("ownID->" + m.group(1));
            return m.group(1);
        } else {//No se encontro coincidencia, guardarlo en un archivo para analizar el problema
            saveFile("I"+timeFormat.format(now)+".tmp", inStr);
            System.out.println("ownID->NotFound");
        }
        return "-1";//No se encontro coincidencia, terminar la ejecución.
    }

    public static String parseUsername(String inStr){
        String pattern = "http\\:\\/\\/www.facebook.com\\/<strong>(.[^<]*)</strong>";

        // Crear un objeto Pattern a partir de la expresión regular
        Pattern r = Pattern.compile(pattern);

        // Aplicar la expresión regular sobre el texto
        Matcher m = r.matcher(inStr);

        //Obtener la fecha actual
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date now=new Date(System.currentTimeMillis());

        if (m.find()) {//Se encontro coincidencia
            System.out.println("Username->" + m.group(1));
            return m.group(1);
        } else {//No se encontro coincidencia, guardarlo en un archivo para analizar el problema
            saveFile("I"+timeFormat.format(now)+".tmp", inStr);
            System.out.println("Username->NotFound");
        }
        return "-1";//No se encontro coincidencia, terminar la ejecución.
    }

    public static boolean getFriends(WebClient webClient, String uid, int nextLevel, HashSet<Amigo> tmpFriends){
        //Preparar URL de amigos. Existen 2 tipos de URL dependiendo del formato del UID obtenido
        String urlFriends = "https://m.facebook.com/"+uid+"/friends?";//IDUsuario Alfanumerico
        if(uid.matches("[0-9]*")){//IDUsuario Numerico
            urlFriends = "https://m.facebook.com/profile.php?v=friends&id="+uid+"&";
        }

        System.out.println("Explorando amigos en:"+urlFriends);
        try {
            boolean newAdditions;
            String respuesta = webClient.getPage(urlFriends).getWebResponse().getContentAsString();//Hacer petición HTTP
            List<String[]> friends = parseFriends(respuesta, 1);//Obtener codigo html de la petición y parsearlo
            newAdditions = saveNewFriends(friends, uid, nextLevel, tmpFriends);//Almacenar los nuevos nodos y aristas

            int startindex=24;
            while( (respuesta.contains("m_more_friends") && respuesta.contains("startindex="+startindex)) //Asegurar que existe una siguiente pagina
                                &&
                   ( nextLevel==1     //En el primer nivel obtener todos los amigos (Sin importar el parametro "amigosMax")
                    || amigosMax == 0  //En caso de que amigosMax sea 0 obtener toda la lista de amigos de cada nodo
                    || startindex<=amigosMax)
                   ){ //Solo obtener los amigos indicados por amigosMax

                System.out.println("Explorando amigos en:"+urlFriends+"startindex="+startindex);
                respuesta = webClient.getPage(urlFriends+"startindex="+startindex).getWebResponse().getContentAsString();//Hacer petición HTTP

                friends = parseFriends(respuesta, 2);//Obtener codigo html de la petición y parsearlo
                newAdditions = saveNewFriends(friends, uid, nextLevel, tmpFriends) || newAdditions;//Almacenar los nuevos nodos y aristas

                System.out.println("Tamaño de la lista amigos ahora es:"+amigosTable.size());
                startindex+=36;//Para acceder a la siguiente página hay que sumar 36
            }
            return newAdditions;
        } catch (Exception e) {
            System.out.println("Obteniendo Amigos:");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveNewFriends(List<String[]> newFriends, String uid, int nextLevel, HashSet<Amigo> tmpFriends){
        boolean newAdditions = false;
        //Iterar nuevos amigos
        for(String[] friend:newFriends){
            if(!amigosTable.containsKey(friend[0])){
                Amigo newFriend = new Amigo(friend[0], friend[1], nextLevel, amigosTable.size()+1);

                tmpFriends.add(newFriend);
                amigosTable.put(friend[0], newFriend);

                System.out.println("AddNode>"+friend[0]+":"+friend[1]);
                newAdditions = true;
            }

            if(!String.valueOf(idNode(uid)).equals(String.valueOf(idNode(friend[0])))){//Evitar relaciones loop (arista que apunta al mismo nodo)
                if(friend[0].equalsIgnoreCase(username)){//En caso de ser amigo del usuario semilla (friend[0]==username) se empleara el uid del usuario semilla
                    linksSet.add(new Link(idNode(uid), idNode(uidUser)));
                }else{
                    linksSet.add(new Link(idNode(uid), idNode(friend[0])));
                }
            }
        }

        return newAdditions;
    }

    public static int idNode(String uid){
        if(uid.equals(username) || uid.equals(uidUser)){
            return 1;
        }

        Amigo amigo = amigosTable.get(uid);
        if(amigo!=null){
            return amigo.numeroNodo;
        }

        return -1;
    }

    public static List<String[]> parseFriends(String inText, int parseType){//Extraer amigos de a partir de un documento HTML
        List<String[]> friends = new LinkedList<>();
        //Expresión regular
        String pattern = "";

        //Seleccionar tipo de parseado
        if(parseType == 1){//Regex para primera pagina de amigos
           pattern = "class=\"c.\" href=\"\\/(profile.php\\?id=)?(.[^\\?\\&\\\"]*).[^>]*>(.[^<]*)<\\/a>";
        }else{//Regex para pagina de amigos con parametro "startindex" a partir de 24
           pattern = "href=\"\\/(profile.php\\?id=)?(.[^\\?\\&\\\"]*).[^>]*>(.[^<]*)<\\/a>";
        }

        //Crear objeto Pattern a partir de la expresión regular
        Pattern r = Pattern.compile(pattern);

        // Encontrar coincidencias en el texto
        Matcher m = r.matcher(inText);

        //Recorrer coincidencias
        while (m.find()) {
            if(!m.group(2).contains(".php") && !m.group(2).contains("/")){ //Mientras no contenga ".php" o "/" es una coincidencia valida
                friends.add(new String[]{m.group(2), m.group(3)});//Agregar a la lista de amigos
            }
        }

        return friends;
    }


    public static void saveFile(String inPath, String content){
        try {
                File file = new File(inPath);

                //Si el archivo no existe crearlo
                if (!file.exists()) {
                        file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(content);
                bw.close();

                System.out.println("Guardado");

        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    public static void saveNodes(){
        try {
            File file = new File("Nodos.csv");
            //Si el archivo no existe crearlo
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            PrintWriter printW = new PrintWriter(fw);

            printW.println("uid,label,id");
            for(Amigo amigo : amigosTable.values()){
                printW.println(amigo.uidFacebook+", "+amigo.nombreUsuario+","+amigo.numeroNodo+"");
            }

            printW.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLinks(){
        try {
            File file = new File("Aristas.csv");
            //Si el archivo no existe crearlo
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            PrintWriter printW = new PrintWriter(fw);

            printW.println("Source,Target,Type");
            for(Link link : linksSet){
                printW.println(""+link.numeroNodoOrigen+","+link.numeroNodoDestino+",undirected");
            }

            printW.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
