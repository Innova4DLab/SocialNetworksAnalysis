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
import static java.lang.String.format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.xalan.lib.ExsltDatetime.time;
import static java.lang.String.format;
import static org.apache.xalan.lib.ExsltDatetime.time;

public class NodeFriends {
    private static List<String[]> amigos;//IDUsuario, NombreUsuario, Nivel, NumeroNodo, estado de exploración "explorado/pendiente"
    private static List<String[]> links;//NumeroNodoOrigen, NumeroNodoDestino 
    
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
        amigos = new LinkedList<>();
        links = new LinkedList<>();
        
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
            
            amigos.add(new String[]{ownID, "Me", "0", "1", "1"});//Agregar a la lista la cuenta semilla
            
            int x=0;//Indice de elemento a explorar
            while(x<amigos.size()){//Recorrer todos los amigos
                int levelNode=Integer.parseInt(amigos.get(x)[2]);//Obtener nivel
                String nodeId=amigos.get(x)[0];//Obtener IDUsuario
                if(levelNode<maxLevel){//Solo explorar los amigos que cumplan con el nivel especificado
                    getFriends(webClient, nodeId, levelNode+1);
                }
                System.out.println("Tamaño de la lista amigos ahora es:"+amigos.size());
                x++;
            }
            
            System.out.println("Lista de todos los amigos:");
            for(String[] node:amigos){
                System.out.println("Amigo> IDUsuario:"+node[0]+", NombreUsuario:"+node[1]);
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
    
    public static void setNodeError(String uid){
        for(int i=0 ;i<amigos.size(); i++){//Buscar el nodo por su uid
            if(amigos.get(i)[0].equalsIgnoreCase(uid)){//Si lo encontramos en la lista de nodos
                amigos.get(i)[4]="-1";//Cambiar su valor de estado por -1
            }
        }
    }
    
    public static String parseOwnID(String inStr){
        String pattern = "\\/composer\\/mbasic\\/\\?av=([0-9]{10,15})";

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
    
    ///icluster.puebla/about
    //Me->100003558519747
    //https://m.facebook.com/profile.php?v=friends&id=100003558519747
    //Icluster      
    //https://m.facebook.com/profile.php?v=friends&id=100011757582987
    public static void getFriends(WebClient webClient, String uid, int nextLevel){
        //Preparar URL de amigos
        String urlFriends = "https://m.facebook.com/"+uid+"/friends?";//IDUsuario Alfanumerico
        if(uid.matches("[0-9]*")){//IDUsuario Numerico
            urlFriends = "https://m.facebook.com/profile.php?v=friends&id="+uid+"&";
        }
        
        System.out.println("Explorando amigos en:"+urlFriends);
        try {
            String respuesta = webClient.getPage(urlFriends).getWebResponse().getContentAsString();//Hacer petición HTTP
            List<String[]> friends = parseFriends(respuesta);//Obtener codigo html de la petición y parsearlo
            saveNewFriends(friends, uid, nextLevel);//Almacenar los nuevos nodos y aristas
            
            int startindex=24;
            while( (respuesta.contains("m_more_friends") && respuesta.contains("startindex="+startindex)) &&
                   ( nextLevel==1 || startindex<amigosMax) ){
                
                System.out.println("Explorando amigos en:"+urlFriends+"startindex="+startindex);               
                respuesta = webClient.getPage(urlFriends+"startindex="+startindex).getWebResponse().getContentAsString();//Hacer petición HTTP

                friends = parseFriendsPage(respuesta);//Obtener codigo html de la petición y parsearlo
                saveNewFriends(friends, uid, nextLevel);//Almacenar los nuevos nodos y aristas

                System.out.println("Tamaño de la lista amigos ahora es:"+amigos.size());
                startindex+=36;
            }
        } catch (Exception e) {
            System.out.println("Obteniendo Amigos:");
            setNodeError(uid);
            e.printStackTrace();
        }
    }   
    
    public static void saveNewFriends(List<String[]> newFriends, String uid, int nextLevel){
        //Iterar nuevos amigos
        for(String[] friend:newFriends){
            if (!idExist(friend[0]) && !friend[0].equalsIgnoreCase(username)){//Agregar amigo si no existe en la sita & Si no es el usuario semilla
                amigos.add(new String[]{friend[0], friend[1], String.valueOf(nextLevel), String.valueOf(amigos.size()+1) ,"1"});
                System.out.println("AddNode>"+friend[0]+":"+friend[1]);
            }

            if(!String.valueOf(idNode(uid)).equals(String.valueOf(idNode(friend[0])))){//Evitar relaciones loop (arista que apunta al mismo nodo) 
                if(friend[0].equalsIgnoreCase(username)){//En caso de ser amigo del usuario semilla se empleara su uid
                    links.add(new String[]{String.valueOf(idNode(uid)), String.valueOf(idNode(uidUser))});
                }else{
                    links.add(new String[]{String.valueOf(idNode(uid)), String.valueOf(idNode(friend[0]))});
                }
            }
        }
    }
    
    public static int idNode(String uid){
        if(uid.equals(username) || uid.equals(uidUser)){
            return 1;
        }
        
        for(int i=0 ;i<amigos.size(); i++){
            if(amigos.get(i)[0].equalsIgnoreCase(uid)){
                return Integer.parseInt(amigos.get(i)[3]);
            }
        }
        return -1;
    }
    
    public static boolean idExist(String element){//Verificar si un IDUsuario existe en la lista de nodos
        for(String[] node:amigos){
            if(node[0].equalsIgnoreCase(element)){
                return true;
            }
        }
        return false;
    }
    
    public static List<String[]> parseFriends(String inText){//Extraer amigos de a partir de un documento HTML
        List<String[]> friends = new LinkedList<>();
        //Expresión regular
        String pattern = "";
    
        pattern = "class=\"c.\" href=\"\\/(profile.php\\?id=)?(.[^\\?\\&\\\"]*).[^>]*>(.[^<]*)<\\/a>";
    
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
    
    public static List<String[]> parseFriendsPage(String inText){//Extraer amigos de a partir de un documento HTML
        List<String[]> friends = new LinkedList<>();
        //Expresión regular
        String pattern = "";

           pattern = "href=\"\\/(profile.php\\?id=)?(.[^\\?\\&\\\"]*).[^>]*>(.[^<]*)<\\/a>";
        
        

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
    static {
      System.out.print("Balance inicial: " );
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
                for(String[] node:amigos){
                    printW.println(node[0]+", "+node[1]+","+node[3]+"");
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
                for(String[] link:links){
                    printW.println(""+link[0]+","+link[1]+",undirected");
                }
                
                printW.close();
                fw.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
    
}
