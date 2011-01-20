/*
 * Copyright (c) 2010. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
package de.betterform.agent.web.servlet;

import de.betterform.agent.web.utils.SortingWalker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:tobias.krebs@betterform.de">tobi</a>
 * @version $Id: FormsServlet 23.11.10 tobi $
 */
public class FormsServlet extends HttpServlet {
    private List ignores;
    private static final String ROOTCOLLECTION = "forms";

    @Override
    public void init() throws ServletException {
        super.init();
        String ignoreString= getInitParameter("ignores");
        ignores = Arrays.asList(ignoreString.split(" "));
    }

    /**
     *  Example URLs:
     *  complete HTML-Listing with header and Footer:
     *  fragment-Listing: /FormBrowser?path=/forms/demo&fragment=true
     *<br><br>
     * fragment with directory links wrapped in an javascript function
     * /FormBrowser?path=/forms/demo&fragment=true&ajax=load
     * will create a link like <a href="#" onclick="load('theUrl');">
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean fragment = false;
        String ajaxFunction = request.getParameter("ajax");
        if(ajaxFunction == null || ajaxFunction.equals("")){
            ajaxFunction="load";
        }
        String fragmentParameter = request.getParameter("fragment");
        String uri = request.getParameter("path");

        if (fragmentParameter != null && fragmentParameter.equals("true")) {
            fragment = true;
        }

        if (!fragment) {
            response.getOutputStream().write(getHTMLHead(request).getBytes());
        }
        response.getOutputStream().write(getHTMLFilesListing(request, uri,ajaxFunction).getBytes());

        if (!fragment) {
            response.getOutputStream().write(getHTMLFooter(request).getBytes());
        }

        response.flushBuffer();
    }

    private static final String getHTMLHead(HttpServletRequest request) {
        return
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xml:lang=\"en\" lang=\"en\">\n" +
                        "<head>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                        "<meta name=\"Author\" content=\"Joern Turner\">\n" +
                        "<meta name=\"Author\" content=\"Lars Windauer\">\n" +
                        "<meta name=\"publisher\" content=\"betterFORM Project\">\n" +
                        "<meta name=\"description\" content=\"betterFORM lighSteelBlue Demo, Reference and XForms 1.1 conformance testsuite browser. Explore the world of XForms\"/>\n" +
                        "<meta name=\"keywords\" content=\"betterFORM, xforms, forms, form, xhtml, xpath, xslt, xslt2.0, dojo, dojotoolkit, java, javascript, xml, schema, web, web2.0, web2, web3.0, web3, semantic, semantic web, joern turner, turner, lars windauer, ajax, xforms processor, processor\">\n" +
                        "<meta name=\"copyright\" content=\"betterForm Project\">\n" +
                        "<meta name=\"content-language\" content=\"en\">\n" +
                        "<meta name=\"robots\" content=\"all\">\n" +
                        "<meta http-equiv=\"expires\" content=\"Wed, 9 Feb 2011 12:21:57 GMT\">\n" +
                        "<meta name=\"revisit-after\" content=\"5 days\">\n" +
                        "        <title>betterFORM lightSteelBlue Forms Browser</title>\n" +
                        "        <link REV=\"made\" HREF=\"mailto:info@betterform.de\"/>\n" +
                        "        <link rel=\"SHORTCUT ICON\" href=\"" + request.getContextPath() + "/images/betterform.ico\"/>\n" +
                        "        <link rel=\"ICON\" href=\"" + request.getContextPath() + "/images/betterform.ico\" type=\"image/x-icon\"/>\n" +
                        "        <link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/styles/bf.css\"/>\n" +
                        "        <link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/resources/styles/betterform-styles.css\"/>\n" +
                        "</head>\n" +
                        "<body id=\"formsbrowser\">\n" +
                        "<div class=\"page\">\n" +
                        "    <div id=\"header\">\n" +
                        "        <div class=\"pageMarginBox\">\n" +
                        "            <div class=\"languages\">\n" +
                        "                <img src=\"" + request.getContextPath() + "/images/en.png\" class=\"langSelector\" alt=\"english\"/>\n" +
                        "                <img src=\"" + request.getContextPath() + "/images/de.png\" class=\"langSelector\" alt=\"deutsch\"/>\n" +
                        "            </div>\n" +
                        "            <div id=\"logoBar\">\n" +
                        "                <a href=\"" + request.getContextPath() + "/index.html\" id=\"linkLogo\" class=\"link\"><img id=\"logo\" src=\"" + request.getContextPath() + "/images/logo.png\" alt=\"betterFORM project\"/></a>\n" +
                        "                <div id=\"topnav\">\n" +
                        "                    <a href=\"" + request.getContextPath() + "/index.html\">home</a><span class=\"menuDevider\"> | </span>\n" +
                        "                    <a href=\"" + request.getContextPath() + "/demo.xhtml\">demo</a><span class=\"menuDevider\"> | </span>\n" +
                        "                    <a href=\"" + request.getContextPath() + "/download.html\">download</a><span class=\"menuDevider\"> | </span>\n" +
                        "                    <a href=\"" + request.getContextPath() + "/product.html\">product</a><span class=\"menuDevider\"> | </span>\n" +
                        "                    <a href=\"" + request.getContextPath() + "/support.html\">support</a><span class=\"menuDevider\"> | </span>\n" +
                        "                    <a href=\"" + request.getContextPath() + "/whoweare.html\">who we are</a>\n" +
                        "                </div>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "\n" +
                        "    <div id=\"content\" class=\"contact\">\n" +
                        "        <img id=\"shadowTop\" src=\"" + request.getContextPath() + "/images/shad_top.jpg\" alt=\"\"/>\n" +
                        "        <div class=\"pageMarginBox\">\n" +
                        "            <div class=\"contentBody\">\n " +
                        "                 <h1>Forms Browser</h1>\n";
    }


    private static final String getHTMLFooter(HttpServletRequest request) {
        return
                "            </div>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "    <div id=\"footer\">\n" +
                        "        <img id=\"shadowBottom\" src=\"" + request.getContextPath() + "/images/shad_bottom.jpg\" alt=\"\"/>\n" +
                        "        <div class=\"pageMarginBox\">\n" +
                        "            <span id=\"bottomMenu\">\n" +
                        "                &#169; 2010 betterFORM" +
                        "                <span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/index.html\">home</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/demo.xhtml\">demo</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/download.html\">download</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/product.html\">product</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/support.html\">support</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/whoweare.html\">who we are</a><span class=\"menuDevider\"> | </span>\n" +
                        "                <a href=\"" + request.getContextPath() + "/contact.html\">contact / impressum</a>\n" +
                        "            </span>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</div>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "    var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");\n" +
                        "    document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));\n" +
                        "</script>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "    try {\n" +
                        "        var pageTracker = _gat._getTracker(\"UA-15044944-1\");\n" +
                        "        pageTracker._trackPageview();\n" +
                        "    } catch(err) {}\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "</html>";
    }


    private String getHTMLFilesListing(HttpServletRequest request, String uri,String ajaxFunction) throws IOException {
        StringBuffer html = new StringBuffer();
        //Todo: Allow something like forms/demo/.. ??
        if (uri == null || uri.contains("..") || !uri.contains(ROOTCOLLECTION)) {
            uri = ROOTCOLLECTION;
        }

        addTableHead(request,html, uri,ajaxFunction);
        handleFileListing(html, request, uri,ajaxFunction);
        html.append(
                "    </div>");

        return html.toString();
    }

    private void addTableHead(HttpServletRequest request,StringBuffer html, String uri,String ajaxFunction) {
        String wrapperStart = ajaxFunction + "('";
        String wrapperEnd = "');";
        StringBuffer crumb=new StringBuffer("");
        String[] steps = uri.split("/");
        String currentPath="";
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i];

            if(i>0){
                currentPath += "/";
            }
            currentPath += step;
            crumb.append("<div");
            if(i+1==steps.length){
                crumb.append(" id=\"current\"");
            }
            crumb.append(" class=\"pathName\">");
            crumb.append("<a href=\"#\" onclick=\"" + wrapperStart + getRequestURI(request, currentPath) + "&amp;fragment=true&amp;ajax=" + ajaxFunction + wrapperEnd + "\">\n");
            crumb.append(step);
            crumb.append("</a>");
            crumb.append("</div>");
            crumb.append(" ");
        }
        String altTextFormUpload = "Upload your form into this collection";
        String altTextCreateCollection = "Create a new collection";
        html.append(
                "<div id=\"bfFormBrowser\">\n" +
                "        <div class=\"formBrowserHead\">\n" +
                "            <div class=\"formBrowserHeader\">\n" + crumb.toString() +
                "        </div>\n" +
                "        <div id=\"commands\">\n" +
//                "            <img src=\"resources/images/add-folder.png\"/>\n" +
                "           <div dojoType=\"dijit.form.DropDownButton\" class=\"createCollectionDropDownButton\">\n" +
                "               <span class=\"label\"><img style=\"height:28px;width:28px;\" src=\"" + request.getContextPath() + "/resources/images/add-folder.png\" alt=\"" + altTextCreateCollection+ "\"></span>\n" +
                "               <div dojoType=\"dijit.TooltipDialog\" name=\"collectionTooltip\" >\n" +
                "                   <form type=\"dijit.form.Form\" name=\"createCollection\" class=\"createCollection\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "                       <input id=\"bfColectionPath\" name=\"bfCollectionPath\" style=\"display:none\" value=\""+ currentPath +"\"> </input>" +
                "                       <b>Collection name:</b>"+
                "                       <input dojoType=\"dijit.form.TextBox\" class=\"bfCollectionName\" name=\"bfCollectionName\" value=\"\"> </input>" +
                "                       <button dojoType=\"dijit.form.Button\" type=\"button\">\n" +
                "                           create\n" +
                "                           <script type=\"dojo/method\" event=\"onClick\" args=\"evt\">\n" +
                "                               // Do something:\n" +
                "                               createCollection();" +
                "                           </script>\n" +
                "                       </button>" +
                "                   </form>\n" +
                "               </div>\n" +
                "           </div>" +

                "           <div dojoType=\"dijit.form.DropDownButton\" class=\"uploadDropDownButton\">\n" +
                "               <span class=\"label\"><img style=\"height:28px;width:28px;\" src=\"" + request.getContextPath() + "/resources/images/add-file.png\" alt=\"" + altTextFormUpload + "\"></span>\n" +
                "               <div dojoType=\"dijit.TooltipDialog\" name=\"uploadTooltip\" >\n" +
                "                   <form type=\"dijit.form.Form\" name=\"upload\" class=\"upload\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "                       <input id=\"bfUploadPath\" name=\"bfUploadPath\" style=\"display:none\" value=\""+ currentPath +"\"> </input>" +
                "                       <input id=\"bfUploadControl\" type=\"file\" name=\"file\" class=\"bfFileUpload\" onchange=\"sendFile();this.blur();dojo.attr(dojo.query('.bfFileUpload')[0],'value','');\"/>\n" +

                "                   </form>\n" +
                "               </div>\n" +
                "           </div>" +
                "        </div>\n" +
                "</div>\n");
        
    }


    private void handleFileListing(StringBuffer html, HttpServletRequest request, String uri,String ajaxFunction) throws IOException {
        String readDir = null;
        String root = null;
        String rootDir = null;

        root = getServletConfig().getServletContext().getRealPath("");
        if (root == null) {
            root = getServletConfig().getServletContext().getRealPath(".");
        }
        rootDir = root + "/";
        readDir = rootDir + uri;

        File filesroot = new File(readDir);
        if (filesroot.exists()) {
            List<File> files = SortingWalker.sortDirsAndFiles(filesroot);

            //if we get more than 8 entries (yes, i know - lets make that configurable later) add a CSS class to switch into list mode
            int amount = files.size();
            final int maxDisplayed=8;
            /*
            create an additional div when we are in listView mode - this is not ideal but easier to implement in the current
            */
//            boolean isListView = amount > maxDisplayed;
            boolean isListView = !ROOTCOLLECTION.equalsIgnoreCase(uri);

            boolean shorten = true;
            if(isListView){
                shorten = false;
                html.append("    <div id=\"bfListView\">\n");
            }

            File f = null;
            String up = null;
            if (files != null) {
                if (uri.indexOf("/") != -1) {
                    up = uri.substring(0, uri.lastIndexOf("/"));
                    handleUp(html, request, up,ajaxFunction,filesroot.getParentFile().getName());
                }
                //process dirs/collections first
                for (File aFile : files) {
                    if (!ignores.contains(aFile.getName())) {
                        f = new File(readDir + "/" + aFile.getName());

                        if (f.isDirectory()) {
                            handleDirectory(html, request, uri, aFile, ajaxFunction,shorten);
                        }
                    }
                }
                for (File aFile : files) {
                    if (!ignores.contains(aFile.getName())) {
                        f = new File(readDir + "/" + aFile.getName());

                        if (f.isFile()) {
                            handleFile(html, request, uri, aFile, f,shorten);
                        }
                    }
                }
//                addFolderLink(html, request, uri);
//                addFileLink(html, request, uri);
            }
            if(isListView){
                html.append("    </div>\n");
            }

        }
    }

    private void handleUp(StringBuffer html, HttpServletRequest request, String up,String ajaxFunction,String parentName) {
        if (ajaxFunction != null) {
            String wrapperStart = ajaxFunction + "('";
            String wrapperEnd = "');";
            html.append(
                    "        <div class=\"directory parent\">\n" +
                            "                <a href=\"#\" onclick=\"" + wrapperStart + getRequestURI(request, up) + "&amp;fragment=true&amp;ajax=" + ajaxFunction + wrapperEnd + "\"><img id=\"go-up\" alt=\"up one level\" src=\"" + request.getContextPath() + "/resources/images/arrow-up.png\" border=\"0\"></a>\n" +
                            "                <a class=\"textLink\" href=\"#\" onclick=\"" + wrapperStart + getRequestURI(request, up)  + "&amp;fragment=true&amp;ajax=" + ajaxFunction + wrapperEnd + "\">" + parentName + "</a>\n" +
                            "        </div>");
        } else {
            html.append(
                    "        <div class=\"directory parent\" >\n" +
                            "                <a href=\"" + getRequestURI(request, up) + "\"><img id=\"go-up\"  alt=\"up one level\" src=\"" + request.getContextPath() + "/resources/images/arrow-up.png\" border=\"0\"></a>\n" +
                            "                <a class=\"textLink\" href=\"" + getRequestURI(request, up) + "\">" + parentName + "</a>\n" +
                            "            </div>\n");
        }
    }

    private void handleDirectory(StringBuffer html, HttpServletRequest request, String uri, File aFile,String ajaxFunction,boolean shortenNames) {
        if (ajaxFunction != null) {
            String wrapperStart = ajaxFunction + "('";
            String wrapperEnd = "');";
            html.append(
                    "        <div class=\"directory\">\n" +
                            "                <a class=\"bfIconDirectory\" href=\"#\" onclick=\"" + wrapperStart + getRequestURI(request, uri) + "/" + aFile.getName()  + "&amp;fragment=true&amp;ajax=" + ajaxFunction + wrapperEnd + "\">\n" +
                            "                   <img src=\"" + request.getContextPath() + "/resources/images/arrow-down.png\" border=\"0\">" +
                            "                </a>\n" +
                            "                <a class=\"textLink\" title=\""+ aFile.getName()+"\" href=\"#\" onclick=\"" + wrapperStart + getRequestURI(request, uri) + "/" + aFile.getName()  + "&amp;fragment=true&amp;ajax=" + ajaxFunction + wrapperEnd + "\">" + getFileName(aFile,shortenNames)+ "</a>\n" +
                            "        </div>");
        } else {
            html.append(
                    "        <div class=\"directory\">\n" +
                            "                <a class=\"bfIconDirectory\" href=\"" + getRequestURI(request, uri) + "/" + aFile.getName() + "\">" +
                            "                   <img src=\"" + request.getContextPath() + "/resources/images/arrow-down.png\" border=\"0\">" +
                        "                   </a>\n" +
                            "                <a class=\"textLink\" href=\"" + getRequestURI(request, uri) + "/" + aFile.getName() + "\">" + getFileName(aFile,shortenNames) + "</a>\n" +
                            "        </div>");
        }

    }

    private void addFolderLink(StringBuffer html, HttpServletRequest request, String uri){
        html.append(
                "        <div class=\"file\">\n" +
                        "       <a class=\"bfIconFile\" href=\"" + request.getContextPath() + "/" + uri + "/addFolder.xhtml" + "\" target=\"_blank\">" +
                        "          <img src=\"" + request.getContextPath() + "/resources/images/add-folder.png"+"\" border=\"0\">\n" +
                        "       </a>\n" +
                        "       <a class=\"textLink\" title=\"add a folder to this collection\" href=\"" + request.getContextPath() + "/" + uri + "/addFolder.xhtml" + "\" target=\"_blank\">" + "add a folder" + "</a>\n" +
                        "       <a class=\"sourceLink\" title=\""+ "view" +"\" href=\"" + request.getContextPath() + "/" + uri + "/addFolder.xhtml" + "?source=true \" target=\"_blank\">" + "<&nbsp;/&nbsp;>" + "</a>\n" +
                        " </div>");
    }

    private void addFileLink(StringBuffer html, HttpServletRequest request, String uri){
        html.append(
                "        <div class=\"file\">\n" +
                        "       <a class=\"bfIconFile\" href=\"" + request.getContextPath() + "/" + uri + "/addFileLink.xhtml" + "\" target=\"_blank\">" +
                        "          <img src=\"" + request.getContextPath() + "/resources/images/add-file.png"+"\" border=\"0\">\n" +
                        "       </a>\n" +
                        "       <a class=\"textLink\" title=\"add a file to this collection\" href=\"" + request.getContextPath() + "/" + uri + "/addFileLink.xhtml" + "\" target=\"_blank\">" + "add a file" + "</a>\n" +
                        "       <a class=\"sourceLink\" title=\""+ "view" +"\" href=\"" + request.getContextPath() + "/" + uri + "/addFileLink.xhtml" + "?source=true \" target=\"_blank\">" + "<&nbsp;/&nbsp;>" + "</a>\n" +
                        " </div>");
    }

    private void handleFile(StringBuffer html, HttpServletRequest request, String uri, File aFile, File f, boolean shortenNames) {


        String fileExtension = aFile.getName().substring(aFile.getName().lastIndexOf(".") +1 , aFile.getName().length()).toUpperCase();
        String iconFile = "standardIcon.png";
        if(fileExtension.equals("XHTML")){
//            iconFile = "bf_logo_square_no_effect_gray.png";
            iconFile = "type-bf.png";
        }else if(fileExtension.equals("TXT")){
            iconFile = "type-txt.png" ;
        }else if(fileExtension.equals("XML")){
            iconFile = "type-xml.png";
        } if(fileExtension.equals("XSD")){
            iconFile = "type-xsd.png";
        }else if(fileExtension.equals("XSL")){
            iconFile = "type-xsl.png";
        }else if(fileExtension.equals("GIF")){
            iconFile = "type-gif.png";
        }else if(fileExtension.equals("PNG")){
            iconFile = "type-png.png";
        }else if(fileExtension.equals("JPG")){
            iconFile = "type-jpg.png";
        }else if(fileExtension.equals("CSS")){
            iconFile = "type-css.png";
        }else if(fileExtension.equals("JS")){
            iconFile = "type-js-png";
        }

        html.append(
                        "        <div class=\"file\">\n" +
                        "                <a class=\"bfIconFile\" href=\"" + request.getContextPath() + "/" + uri + "/" + aFile.getName() + "\" target=\"_blank\">" +
                        "                   <img src=\"" + request.getContextPath() + "/resources/images/"+iconFile+"\" border=\"0\">\n" +
                        "                </a>\n" +
                        "                <a class=\"textLink\" title=\""+ aFile.getName()+"\" href=\"" + request.getContextPath() + "/" + uri + "/" + aFile.getName() + "\" target=\"_blank\">" + getFileName(aFile,shortenNames) + "</a>\n" +
                        "                <a class=\"sourceLink\" title=\""+ "view" +"\" href=\"" + request.getContextPath() + "/" + uri + "/" + aFile.getName() + "?source=true \" target=\"_blank\">" + "<&nbsp;/&nbsp;>" + "</a>\n" +
/*
                        "            <div>\n" +
                        "                <a href=\"" + request.getContextPath() + "/" + uri + "/" + aFile.getName() + "?source=true\" target=\"_blank\">source</a>\n" +
                        "            </div>\n" +
                        "            <div>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(f.lastModified())) + "</div>\n" +
*/
                        "        </div>");
    }

    private String getRequestURI(HttpServletRequest request, String path) {
        return request.getRequestURI() + "?path=" + path;
    }

    private String getFileName(File aFile,boolean shortenNames) {
        String fileName = aFile.getName();
        if(fileName.indexOf(".xhtml") != -1 ){
            fileName = fileName.replace(".xhtml","");
        }

        if(shortenNames){
            if(fileName.length() > 15){
                fileName = fileName.substring(0,10) + "..." + fileName.substring(fileName.length()-5);
            }
            return fileName;
        }else {
            return fileName;
        }
    }
}
