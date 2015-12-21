package modules.export.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import control.result.ResultFragment;
import control.result.Type;
import modules.cms.CMSController;
import modules.cms.SessionHolder;
import modules.export.Export;
import modules.export.Fragment;
import org.docx4j.Docx4jProperties;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.vml.*;
import org.docx4j.wml.*;
import org.docx4j.wml.ObjectFactory;
import play.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Bendikt Linke on 12.12.2015.
 */
public class DocxExport implements Export {

    String path;
    String fileName;

    WordprocessingMLPackage wordMLPackage;
    MainDocumentPart mainDocumentPart;


    @Override
    public void initialize(String path, String fileName, boolean landscape) {
        this.path = path;
        this.fileName = fileName;

        try {
            wordMLPackage = WordprocessingMLPackage.createPackage();
            mainDocumentPart = wordMLPackage.getMainDocumentPart();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        }

        setOrientation(landscape);
        setPageMargins(0,0,0,0);

    }

    @Override
    public void export(ResultFragment fragment) {
        double startX = fragment.getStartX() * 1000;
        double startY = fragment.getStartY() * 1000;
        double width = (fragment.getEndX() - fragment.getStartX()) * 1000;
        double height = (fragment.getEndY() - fragment.getStartY()) * 1000;


        String style ="position:absolute;" +
                "mso-wrap-style:square;" +
                "mso-width-percent:"+ width + ";" +
                "mso-height-percent:"+ height + ";" +
                "mso-left-percent:"+ startX + ";" +
                "mso-top-percent:"+ startY + ";";

        P p = new P();
        mainDocumentPart.addObject(p);


        if(fragment.getType() == Type.TEXT) {
            R r = Context.getWmlObjectFactory().createR();
            r.getContent().add(createTextBox(style, createContent(((String) fragment.getResult()))));
            p.getContent().add(r);
        }else{
            R r = Context.getWmlObjectFactory().createR();
            try {
                r.getContent().add(createTextBox(style, createImageInTextBox((BufferedImage)fragment.getResult(), (long) width * 9, (long) height)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            p.getContent().add(r);
        }

    }

    /**
     * Adds a page break to the document.
     */
    @Override
    public void newPage() {
        Br breakObj = new Br();
        breakObj.setType(STBrType.PAGE);

        P paragraph = Context.getWmlObjectFactory().createP();
        paragraph.getContent().add(breakObj);
        try {
            mainDocumentPart.getContents().getBody().getContent().add(paragraph);
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File finish() {
        File file = new File(path + fileName + ".docx");

        try {
            wordMLPackage.save(file);
        } catch (Docx4JException e) {
            e.printStackTrace();
        }

        return file;
    }

    // Create a TextBox inside a Picture
    private JAXBElement createTextBox(String style, P textboxContent) {
        org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

        Pict pict = wmlObjectFactory.createPict();
        JAXBElement<org.docx4j.wml.Pict> pictWrapped = wmlObjectFactory.createRPict(pict);
        org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();

        // Create object for shapetype (wrapped in JAXBElement)
        CTShapetype shapetype = vmlObjectFactory.createCTShapetype();
        JAXBElement<org.docx4j.vml.CTShapetype> shapetypeWrapped = vmlObjectFactory.createShapetype(shapetype);
        pict.getAnyAndAny().add( shapetypeWrapped);

        //define a textfield instead of autoform
        shapetype.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
        shapetype.setSpt( new Float(202.0) );
        shapetype.setConnectortype(org.docx4j.vml.officedrawing.STConnectorType.STRAIGHT);

        // Create object for path (wrapped in JAXBElement)
        CTPath path = vmlObjectFactory.createCTPath();
        JAXBElement<org.docx4j.vml.CTPath> pathWrapped = vmlObjectFactory.createPath(path);
        shapetype.getEGShapeElements().add( pathWrapped);
        path.setGradientshapeok(org.docx4j.vml.STTrueFalse.T);
        path.setConnecttype(org.docx4j.vml.officedrawing.STConnectType.RECT);
        shapetype.setCoordsize( "21600,21600");
        shapetype.setVmlId( "_x0000_t202");
        shapetype.setHralign(org.docx4j.vml.officedrawing.STHrAlign.LEFT);
        shapetype.setPath( "m,l,21600r21600,l21600,xe");

        // Create object for shape (wrapped in JAXBElement)
        CTShape shape = vmlObjectFactory.createCTShape();
        JAXBElement<org.docx4j.vml.CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);

        pict.getAnyAndAny().add( shapeWrapped);

        shape.setStyle( style);
        shape.setSpid( "_x0000_s1026");

        // Remove the Stroke around textfield
        shape.setStroked(STTrueFalse.FALSE);
        shape.setFilled(STTrueFalse.FALSE);


        // Create object for textbox (wrapped in JAXBElement)
        CTTextbox textbox = vmlObjectFactory.createCTTextbox();
        JAXBElement<org.docx4j.vml.CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox);
        shape.getEGShapeElements().add( textboxWrapped);
        textbox.setStyle( style);
        textbox.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
        // Create object for txbxContent
        CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
        textbox.setTxbxContent(txbxcontent);


        txbxcontent.getContent().add(textboxContent);


        shape.setVmlId("TextBox");
        shape.setHralign(org.docx4j.vml.officedrawing.STHrAlign.LEFT);
        shape.setType( "#_x0000_t202");
        // <w10:wrap type="topAndBottom"/>
        return pictWrapped;
    }

    private P createContent(String textContent) {

        P p = Context.getWmlObjectFactory().createP();

        R r = Context.getWmlObjectFactory().createR();
        p.getContent().add(r);

        String[] lines = textContent.split("\n");
        for(String line : lines) {
            // Create object for t (wrapped in JAXBElement)
            Text text = Context.getWmlObjectFactory().createText();
            JAXBElement<org.docx4j.wml.Text> textWrapped = Context.getWmlObjectFactory().createRT(text);
            r.getContent().add( textWrapped);
            text.setValue(line.trim());

            Br br = Context.getWmlObjectFactory().createBr(); // this Br element is used break the current and go for next line
            r.getContent().add(br);
        }

        return p;
    }

    private P createImageInTextBox(BufferedImage content, long width, long heigth) throws Exception {

        P p = Context.getWmlObjectFactory().createP();

        String filenameHint = null;
        String altText = null;
        int id1 = 0;
        int id2 = 1;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes;
        ImageIO.write(content, "png", baos);
        imageBytes = baos.toByteArray();

        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, imageBytes);
        Logger.info("calculated image width: " + width);
        Inline inline = imagePart.createImageInline( filenameHint, altText, id1, id2, width, false);

        // Now add the inline in w:p/w:r/w:drawing
        org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

        R  run = factory.createR();
        Drawing drawing = factory.createDrawing();
        run.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        p.getContent().add(run);

        return p;
    }


    // Add Metadata
    private void addCustomDocProp(String key, String value){
        DocPropsCustomPart docPropsCustomPart = null;
        try {
            docPropsCustomPart = new DocPropsCustomPart();
            wordMLPackage.addTargetPart(docPropsCustomPart);
            org.docx4j.docProps.custom.ObjectFactory cpfactory = new org.docx4j.docProps.custom.ObjectFactory();
            org.docx4j.docProps.custom.Properties customProps = cpfactory.createProperties();
            docPropsCustomPart.setJaxbElement(customProps);

            // Ok, let's add a custom property.
            org.docx4j.docProps.custom.Properties.Property newProp = cpfactory.createPropertiesProperty();
            newProp.setName(key);
            newProp.setFmtid(docPropsCustomPart.fmtidValLpwstr ); // Magic string
            newProp.setPid( customProps.getNextId() );
            newProp.setLpwstr(value);

            // .. add it
            customProps.getProperty().add(newProp);

        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }


    private void setPageMargins(int bottom, int top, int left, int right) {
        try {
            Body body = mainDocumentPart.getContents().getBody();
            PageDimensions page = new PageDimensions();
            SectPr.PgMar pgMar = page.getPgMar();
            pgMar.setBottom(BigInteger.valueOf(bottom));
            pgMar.setTop(BigInteger.valueOf(top));
            pgMar.setLeft(BigInteger.valueOf(left));
            pgMar.setRight(BigInteger.valueOf(right));
            ObjectFactory factory = Context.getWmlObjectFactory();

            SectPr sectPr = factory.createSectPr();
            body.setSectPr(sectPr);
            sectPr.setPgMar(pgMar);

        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }


    //TODO: doesn't work
    private void setOrientation(boolean landscape){
        Properties properties = Docx4jProperties.getProperties();
        properties.setProperty("docx4j.PageOrientationLandscape", Boolean.toString(landscape));
    }
}
