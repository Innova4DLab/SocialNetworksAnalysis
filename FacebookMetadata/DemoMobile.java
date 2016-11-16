
package publybot;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookException;
import com.restfb.types.Page;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Scanner;

/**
 *
 * @author joseluisvzg@gmail.com
 */
public class DemoMobile {
    private static WebClient webClient;
    private static String email="HereUsername", password="HerePassword";//Credenciales de una cuenta Facebook
     
    public static void main(String[] args){
        email=PublyBot.email;
        password=PublyBot.password;
        
        if(initFacebook()){
            String friend = "sofiafernandagoiz";
            HtmlPage about = getAbout(webClient, friend);//Visit about page of friend
            parseAbout(about.getWebResponse().getContentAsString());
        }
    }

        public static void parseAbout(String text){
         HtmlPage page=null;
        try {
             URL url = new URL("http://www.example.com");
             StringWebResponse response = new StringWebResponse(text, url);
             page = HTMLParser.parseHtml(response, webClient.getCurrentWindow());           
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Información Basica:");
        List<HtmlElement> tags = (List<HtmlElement>)page.getByXPath("//div[@id='basic-info']//div[@title]/div/div[1]");
        //Iterate tags with "edu_work" attribute
        for (HtmlElement xmlEdu_work : tags) {
                System.out.println(xmlEdu_work.asText());
        }
        System.out.println("Trabajo:");
        tags = (List<HtmlElement>)page.getByXPath("//div[@id='work']//span/a");
        //Iterate tags with "work" attribute
        for (HtmlElement xmlEdu_work : tags) {
                System.out.println(xmlEdu_work.asText());
        }
        System.out.println("");
        tags = (List<HtmlElement>)page.getByXPath("//div[@id='education']");
        //Iterate tags with "education" attribute
        for (HtmlElement xmlEdu_work : tags) {
             System.out.println(xmlEdu_work.asText());
        }
        System.out.println("");
        tags = (List<HtmlElement>)page.getByXPath("//div[@id='living']");
        //Iterate tags with "living" attribute
        for (HtmlElement xmlEdu_work : tags) {
             //System.out.println(xmlEdu_work.getAttribute("href"));
             System.out.println(xmlEdu_work.asText());
        }
        System.out.println("");
        tags = (List<HtmlElement>)page.getByXPath("//div[@id='relationship']");
        //Iterate tags with "relationship" attribute
        for (HtmlElement xmlEdu_work : tags) {
             System.out.println(xmlEdu_work.asText());
        }
    }

    public static HashSet<String> parsePagesIds(String xml){
        //System.out.println("ParsePage:"+xml);
        HashSet<String> pages = new HashSet<>();
        Pattern p = Pattern.compile("hovercard\\/page\\.php\\?id=([0-9]{10,20})");
        Matcher m = p.matcher(xml);
        while (m.find()) {
           pages.add(m.group(1));
        }
        return pages;
    }
    
    public static HtmlPage getAbout(WebClient webClient, String uid){
   
  //          webClient.setJavaScriptTimeout(10000); //e.g. 30s
        
        //Preparar URL de amigos
        String urlFriends = "https://m.facebook.com/"+uid+"/about";//IDUsuario Alfanumerico
        if(uid.matches("[0-9]*")){//IDUsuario Numerico
            urlFriends = "https://www.facebook.com/profile.php?v=about&id="+uid+"";
        }
        
        System.out.println("Explorando About en:"+urlFriends);
        try {
            return webClient.getPage(urlFriends);//Hacer petición HTTP
        } catch (Exception e) {
            System.out.println("Error obteniendo About:");
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean initFacebook(){
         try {
            //Crear un objeto WebClient
            webClient = new WebClient();
            //Abrir la pagina facebook dentro del Webclient
            final HtmlPage page1 = webClient.getPage("https://www.facebook.com/");
            //Seleccionar el formulario de inicio de sesión
            final HtmlForm form = (HtmlForm) page1.getElementById("login_form");
            //Seleccionar el boton de inicio de sesión
            final HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Log In").get(0);
            //Seleccionar la caja de texto "email" y ingresarle el email de la cuenta semilla
            final HtmlTextInput textField = form.getInputByName("email");
            textField.setValueAttribute(email);
            //Seleccionar la caja de texto "pass" y ingresarle la contraseña de la cuenta semilla
            final HtmlPasswordInput textField2 = form.getInputByName("pass");
            textField2.setValueAttribute(password);
            //Iniciar sesión
            final HtmlPage page2 = button.click();
            
            //Obtener el IDUsuario de cuenta semilla
            final HtmlPage htmlID = webClient.getPage("https://m.facebook.com/home.php");
            String ownID = parseOwnID(htmlID.getWebResponse().getContentAsString());
          
            if(ownID.equals("-1")){
                System.out.println("Credenciales incorrectas!!!");
                return false;//IDUsuario no encontrado (credeciales incorrectas)
            }            
           
        } catch (Exception e) {
            e.printStackTrace();
             System.out.println("Error on InitFacebook");
            return false;//Ocurrio un error!!!
        } 
        return true;
    }
    
 public static String parseOwnID(String inStr){
        String pattern = "\\\\/composer\\\\/mbasic\\\\/\\?av=([0-9]+)";//\\/composer\\/mbasic\\/\?av=([0-9]{10,15})

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
            System.out.println("ownID->NotFound");
        }
        return "-1";//No se encontro coincidencia, terminar la ejecución.
    }

}
