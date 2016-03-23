package docureams.forms.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

public class FormType implements Serializable {
    private long id;

    private String name;
    
    private String description;

    private String jsonMetadata;

    private String pdfTemplate;

    private static class PDFieldDescriptor {
        public String fullyQualifiedFieldName;
        public String alternateFieldName;
        public String fieldType;
        public PDFieldDescriptor(String fullyQualifiedFieldName, String alternateFieldName, String fieldType) {
            this.fullyQualifiedFieldName = fullyQualifiedFieldName;
            this.alternateFieldName = alternateFieldName;
            this.fieldType = fieldType;
        }
    }

    private HashMap<String, PDFieldDescriptor> _metadataMap;
    private HashMap<String, PDFieldDescriptor> metadataMap() throws IOException {
        if (_metadataMap == null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference metadataType = new TypeReference<HashMap<String, PDFieldDescriptor>>() { };
            _metadataMap = mapper.readValue(jsonMetadata, metadataType);  
        }
        return _metadataMap;
    }

    private HashMap<String, String> _reverseMap;
    private HashMap<String, String> reverseMap() throws IOException {
        if (_reverseMap == null) {
            _reverseMap = new HashMap<>();
            for (String fieldKey : metadataMap().keySet()) {
                PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                _reverseMap.put(fieldDesc.fullyQualifiedFieldName, fieldKey);
            }
        }
        return _reverseMap;
    }

    public FormType() {
    }

    public FormType(String name, String description, String jsonMetadata, String pdfTemplate) {
        this.name = name;
        this.description = description;
        this.jsonMetadata = jsonMetadata;
        this.pdfTemplate = pdfTemplate;
    }
    
    public long getId() {
        return id;
    }

    public FormType setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FormType setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FormType setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getJsonMetadata() {
        return jsonMetadata;
    }

    public FormType setJsonMetadata(String jsonMetadata) {
        this.jsonMetadata = jsonMetadata;
        return this;
    }

    public String getPdfTemplate() {
        return pdfTemplate;
    }

    public FormType setPdfTemplate(String pdfTemplate) {
        this.pdfTemplate = pdfTemplate;
        return this;
    }

    public InputStream generatePdf(String jsonData) throws Exception {
        return new GeneratePdfJob(jsonData).doJobWithResult();
    }

    public String parsePdf(File pdfFile) {
        return new ParsePdfJob(pdfFile).doJobWithResult();
    }

    public static String parseMetadataFromPdf(File pdfFile) {
        return new ParseMetadataFromPdfJob(pdfFile).doJobWithResult();
    }
    
    public String defaults() throws JsonProcessingException, IOException {
        HashMap<String, Object> dataMap = new HashMap<>();
        for (String fieldKey : metadataMap().keySet()) {
            PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
            dataMap.put(fieldKey, fieldDesc.fieldType.endsWith("PDCheckbox") ? false : "");
        }

        return new ObjectMapper().writeValueAsString(dataMap);
    }
    
    public String toJson() {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\"}", name, description);
    }

    public class GeneratePdfJob {

        private final HashMap<String, Object> dataMap;

        public GeneratePdfJob(String jsonData) throws IOException {
            TypeReference dataType = new TypeReference<HashMap<String, Object>>() {};
            this.dataMap = new ObjectMapper().readValue(jsonData, dataType);
        }

        public InputStream doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(new File(pdfTemplate));
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm != null) {
                    for (String fieldKey : this.dataMap.keySet()) {
                        PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                        if (fieldDesc.fieldType.endsWith("PDCheckbox")) {
                            PDCheckbox field = (PDCheckbox) acroForm.getField(fieldDesc.fullyQualifiedFieldName);
                            if ((Boolean)(this.dataMap.get(fieldKey))) {
                                field.check();
                            } else {
                                field.unCheck();
                            }
                        } else if (fieldDesc.fieldType.endsWith("PDTextbox")) {
                            PDTextbox field = (PDTextbox) acroForm.getField(fieldDesc.fullyQualifiedFieldName);
                            field.setValue(this.dataMap.get(fieldKey).toString());
                        }
                    }
                }

                // Save and close the filled out form.
                File tempFile = File.createTempFile("form", ".pdf");
                tempFile.deleteOnExit();
                document.save(tempFile.getCanonicalPath());

                return new FileInputStream(tempFile);
            } catch (Exception ex) {
                Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public class ParsePdfJob {

        private final File pdfFile;
        private final HashMap<String, Object> dataMap = new HashMap<>();

        public ParsePdfJob(File pdfFile) {
            this.pdfFile = pdfFile;
        }

        public String doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfFile);
                PDDocumentCatalog docCatalog = document.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                List fields = acroForm.getFields();
                Iterator fieldsIter = fields.iterator();
                while (fieldsIter.hasNext()) {
                    PDField field = (PDField) fieldsIter.next();
                    processField(field, field.getPartialName());
                }
                return new ObjectMapper().writeValueAsString(dataMap);
            } catch (Exception ex) {
                return null;
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        private void processField(PDField field, String sParent) throws IOException {
            List<COSObjectable> kids = field.getKids();
            String partialName = field.getPartialName();
            if (kids != null) {
                Iterator<COSObjectable> kidsIter = kids.iterator();
                if (!sParent.equals(partialName)) {
                    if (partialName != null) {
                        sParent = sParent + "." + partialName;
                    }
                }
                while (kidsIter.hasNext()) {
                    Object pdfObj = kidsIter.next();
                    if (pdfObj instanceof PDField) {
                        PDField kid = (PDField) pdfObj;
                        processField(kid, sParent);
                    }
                }
            } else {
                Object fieldValue = null;
                if (field instanceof PDTextbox) {
                    fieldValue = field.getValue();
                } else if (field instanceof PDCheckbox) {
                    fieldValue = ((PDCheckbox) field).isChecked();
                }
                
                if (fieldValue != null) {
                    dataMap.put(reverseMap().get(field.getFullyQualifiedName()), fieldValue);
                }
            }
        }
    }
    
    public static class ParseMetadataFromPdfJob {

        private final File pdfFile;
        private final StringBuilder outputString = new StringBuilder();

        public ParseMetadataFromPdfJob(File pdfFile) {
            this.pdfFile = pdfFile;
        }

        public String doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfFile);
                PDDocumentCatalog docCatalog = document.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                List fields = acroForm.getFields();
                outputString.append("{\n");
                Iterator fieldsIter = fields.iterator();
                while (fieldsIter.hasNext()) {
                    PDField field = (PDField) fieldsIter.next();
                    processField(field, field.getPartialName());
                    if (fieldsIter.hasNext()) {
                        outputString.append(",");
                    }
                }
                outputString.append("}\n");
                document.close();
                return outputString.toString();
            } catch (Exception ex) {
                Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        private void processField(PDField field, String sParent) throws IOException {
            List<COSObjectable> kids = field.getKids();
            String partialName = field.getPartialName();
            if (kids != null) {
                Iterator<COSObjectable> kidsIter = kids.iterator();
                if (!sParent.equals(partialName)) {
                    if (partialName != null) {
                        sParent = sParent + "." + partialName;
                    }
                }
                while (kidsIter.hasNext()) {
                    Object pdfObj = kidsIter.next();
                    if (pdfObj instanceof PDField) {
                        PDField kid = (PDField) pdfObj;
                        processField(kid, sParent);
                    }
                }
            } else {
                String alternateFieldName = field.getAlternateFieldName();
                String fieldValue = null;
                if (field instanceof PDTextbox) {
                    fieldValue = field.getValue();
                } else if (field instanceof PDCheckbox) {
                    fieldValue = "checkbox";
                }
                
                outputString.append("\"").append(fieldValue).append("\":{\"fullyQualifiedFieldName\":\"").append(sParent);
                if (partialName != null)
                {
                    outputString.append(".").append(partialName);
                }
                outputString.append("\", \"alternateFieldName\":\"").append(alternateFieldName == null ? "" : alternateFieldName).append("\", ");
                outputString.append("\"fieldType\":\"").append(field.getClass().getName()).append("\"}\n");
            }
        }
    }
}