package docureams.forms.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

public class FormType implements Serializable {
    private long id;

    private String name;
    
    private String description;
    
    private InputStream pdfTemplate;
    
    private String pageFilter = "all";

    private String jsonMetadata;

    private transient List<Integer> _unwantedPages;
    private List<Integer> unwantedPages(int pageCount) {
        if (_unwantedPages == null) {
            _unwantedPages = new ArrayList<>();
            if (!"all".equalsIgnoreCase(pageFilter)) {
                Pattern pattern = Pattern.compile(pageFilter);
                for (Integer idx = pageCount; idx > 0; idx--) {
                    if (!pattern.matcher(idx.toString()).matches()) {
                        _unwantedPages.add(idx-1);
                    }
                }
            }
        }
        return _unwantedPages;
    }

    private static class PDFieldDescriptor {
        public String fullyQualifiedFieldName;
        public String alternateFieldName;
        public String fieldType;
        
        public PDFieldDescriptor() {
        }
        
        public PDFieldDescriptor(String fullyQualifiedFieldName, String alternateFieldName, String fieldType) {
            this.fullyQualifiedFieldName = fullyQualifiedFieldName;
            this.alternateFieldName = alternateFieldName;
            this.fieldType = fieldType;
        }

        public String getFullyQualifiedFieldName() {
            return fullyQualifiedFieldName;
        }

        public void setFullyQualifiedFieldName(String fullyQualifiedFieldName) {
            this.fullyQualifiedFieldName = fullyQualifiedFieldName;
        }

        public String getAlternateFieldName() {
            return alternateFieldName;
        }

        public void setAlternateFieldName(String alternateFieldName) {
            this.alternateFieldName = alternateFieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
    }

    private transient LinkedHashMap<String, PDFieldDescriptor> _metadataMap;
    private LinkedHashMap<String, PDFieldDescriptor> metadataMap() throws IOException {
        if (_metadataMap == null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference metadataType = new TypeReference<LinkedHashMap<String, PDFieldDescriptor>>() { };
            _metadataMap = mapper.readValue(jsonMetadata, metadataType);  
        }
        return _metadataMap;
    }

    private transient LinkedHashMap<String, String> _reverseMap;
    private LinkedHashMap<String, String> reverseMap() throws IOException {
        if (_reverseMap == null) {
            _reverseMap = new LinkedHashMap<>();
            for (String fieldKey : metadataMap().keySet()) {
                PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                _reverseMap.put(fieldDesc.fullyQualifiedFieldName, fieldKey);
            }
        }
        return _reverseMap;
    }

    public FormType() {
    }

    public FormType(String name, String description, String jsonMetadata, InputStream pdfTemplate, String pageFilter) {
        this.name = name;
        this.description = description;
        this.jsonMetadata = jsonMetadata;
        this.pdfTemplate = pdfTemplate;
        this.pageFilter = pageFilter;
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

    @JsonIgnore
    public InputStream getPdfTemplate() {
        return pdfTemplate;
    }

    @JsonIgnore
    public FormType setPdfTemplate(InputStream pdfTemplate) {
        this.pdfTemplate = pdfTemplate;
        return this;
    }
    
    public String getPageFilter() {
        return pageFilter;
    }

    public FormType setPageFilter(String pageFilter) {
        this.pageFilter = pageFilter;
        return this;
    }

    public PDDocument generatePdf(String jsonData) throws Exception {
        return new GeneratePdfJob(jsonData).doJobWithResult();
    }

    public String parsePdf(InputStream pdfInputStream) {
        return new ParsePdfJob(pdfInputStream).doJobWithResult();
    }

    public static String parseMetadataFromPdf(InputStream pdfInputStream) {
        return new ParseMetadataFromPdfJob(pdfInputStream).doJobWithResult();
    }
    
    public String defaults() throws JsonProcessingException, IOException {
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        for (String fieldKey : metadataMap().keySet()) {
            PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
            dataMap.put(fieldKey, fieldDesc.fieldType.endsWith("PDCheckBox") ? false : "");
        }

        return new ObjectMapper().writeValueAsString(dataMap);
    }
    
    private class vbType {
        String typeName;
        boolean isObject = false;
        boolean isArray = false;
        int ubound = 0;
    }
    
    public String generateClientSdkForAsp() {
        try {
            LinkedHashMap<String, LinkedHashMap<String, vbType>> types = new LinkedHashMap<>();
            types.put(name.toUpperCase(), new LinkedHashMap<String, vbType>());
            for (String fieldKey : metadataMap().keySet()) {
                String currentTypeName = name.toUpperCase();
                PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                StringTokenizer st = new StringTokenizer(fieldKey, ".");
                while (st.hasMoreTokens()) {
                    String memberName;
                    vbType vbt = new vbType();
                    String token = st.nextToken();
                    if (token.endsWith("]")) {
                        vbt.isArray = true;
                        int index = Integer.parseInt(token.substring(token.indexOf("[") + 1, token.indexOf("]")));
                        vbt.ubound = index > vbt.ubound ? index : vbt.ubound;
                    }
                    if (st.hasMoreTokens()) {
                        memberName = token.substring(0, token.indexOf("["));
                        vbt.typeName = name.toUpperCase() + "_" + memberName;
                        vbt.isObject = true;
                        if (types.get(vbt.typeName) == null) {
                            types.put(vbt.typeName, new LinkedHashMap<String, vbType>());
                        }
                    } else if (fieldDesc.fieldType.endsWith("PDCheckBox")) {
                        memberName = token;
                        vbt.typeName = "Boolean";
                    } else {
                        memberName = token;
                        vbt.typeName = "String";
                    }
                    types.get(currentTypeName).put(memberName, vbt);
                    currentTypeName = vbt.typeName;
                }
            }
            
            StringBuilder builder = new StringBuilder();
            builder.append("<%\n\n");
            builder.append("Class ").append(name.toUpperCase()).append("\n\n");
            
            for (String memberName : types.get(name.toUpperCase()).keySet()) {
                vbType vbt = types.get(name.toUpperCase()).get(memberName);
                builder.append("  Public ").append(memberName).append(vbt.isArray ? "(" + Integer.toString(vbt.ubound) + ")" : "").append("\n");
            }
            builder.append("\n  Private Sub Class_Initialize()\n");
            for (String memberName : types.get(name.toUpperCase()).keySet()) {
                vbType vbt = types.get(name.toUpperCase()).get(memberName);
                if (vbt.isObject) {
                    if (vbt.isArray) {
                        for (int i = 0; i <= vbt.ubound; i++) {
                            builder.append("    Set ").append(memberName).append("(").append(Integer.toString(i)).append(") = new ").append(vbt.typeName).append("\n");
                        }
                    } else {
                        builder.append("    Set ").append(memberName).append(" = new ").append(vbt.typeName).append("\n");
                    }
                }
            }
            for (String fieldKey : metadataMap().keySet()) {
                PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                builder.append("    ").append(fieldKey.replace('[','(').replace(']',')')).append(" = ").append(fieldDesc.fieldType.endsWith("PDCheckBox") ? "False\n" : "\"\"\n");
            }
            builder.append("  End Sub\n\n");
            builder.append("  Public Function toDictionary()\n");
            builder.append("    Dim data\n");
            builder.append("    Set data = Server.CreateObject(\"Scripting.Dictionary\")\n");
            for (String fieldKey : metadataMap().keySet()) {
                builder.append("    data.Add \"").append(fieldKey).append("\", ").append(fieldKey.replace('[','(').replace(']',')')).append("\n");
            }
            builder.append("    Set toDictionary = data\n");
            builder.append("  End Function\n\n");
            builder.append("  Public Sub fromDictionary(data)\n");
            for (String fieldKey : metadataMap().keySet()) {
                builder.append("    ").append(fieldKey.replace('[','(').replace(']',')')).append(" = ").append("data.Item(\"").append(fieldKey).append("\")\n");
            }
            builder.append("  End Sub\n\n");
            builder.append("End Class\n\n");
            
            for (String typeName : types.keySet()) {
                if (!typeName.equalsIgnoreCase(name)) {
                    builder.append("Class ").append(typeName).append("\n\n");
                    for (String memberName : types.get(typeName).keySet()) {
                        vbType vbt = types.get(typeName).get(memberName);
                        builder.append("  Public ").append(memberName).append(vbt.isArray ? "(" + Integer.toString(vbt.ubound) + ")" : "").append("\n");
                    }
                    builder.append("\nEnd Class\n\n");
                }
            }
            builder.append("%>\n");
            return builder.toString();
        } catch (IOException ex) {
            Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String toJson() {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\"}", name, description);
    }

    public class GeneratePdfJob {

        private final LinkedHashMap<String, Object> dataMap;

        public GeneratePdfJob(String jsonData) throws IOException {
            TypeReference dataType = new TypeReference<LinkedHashMap<String, Object>>() {};
            this.dataMap = new ObjectMapper().readValue(jsonData, dataType);
        }

        public PDDocument doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfTemplate);
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm != null) {
                    for (String fieldKey : this.dataMap.keySet()) {
                        PDFieldDescriptor fieldDesc = metadataMap().get(fieldKey);
                        if (fieldDesc.fieldType.endsWith("PDCheckBox")) {
                            PDCheckBox field = (PDCheckBox) acroForm.getField(fieldDesc.fullyQualifiedFieldName);
                            if ((Boolean)(this.dataMap.get(fieldKey))) {
                                field.check();
                            } else {
                                field.unCheck();
                            }
                        } else if (fieldDesc.fieldType.endsWith("PDTextField")) {
                            PDTextField field = (PDTextField) acroForm.getField(fieldDesc.fullyQualifiedFieldName);
                            Object value = this.dataMap.get(fieldKey);
                            field.setValue(value != null ? value.toString() : "");
                        }
                    }
                }
                for (int idx : unwantedPages(document.getNumberOfPages())) {
                    document.removePage(idx);
                }
                return document;
            } catch (Exception ex) {
                Logger.getLogger(FormType.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    public class ParsePdfJob {

        private final InputStream pdfInputStream;
        private final LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();

        public ParsePdfJob(InputStream pdfInputStream) {
            this.pdfInputStream = pdfInputStream;
        }

        public String doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfInputStream);
                PDDocumentCatalog docCatalog = document.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                for (PDField field : acroForm.getFieldTree()) {
                    processField(field);
                }
                return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dataMap);
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

        private void processField(PDField field) throws IOException {
            Object fieldValue = null;
            if (field instanceof PDTextField) {
                fieldValue = field.getValueAsString();
            } else if (field instanceof PDCheckBox) {
                fieldValue = ((PDCheckBox) field).isChecked();
            }

            if (fieldValue != null) {
                dataMap.put(reverseMap().get(field.getFullyQualifiedName()), fieldValue);
            }
        }
    }
    
    public static class ParseMetadataFromPdfJob {

        private final InputStream pdfInputStream;
        private final StringBuilder outputString = new StringBuilder();

        public ParseMetadataFromPdfJob(InputStream pdfInputStream) {
            this.pdfInputStream = pdfInputStream;
        }

        public String doJobWithResult() {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfInputStream);
                PDDocumentCatalog docCatalog = document.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                outputString.append("{\n");
                PDField field = null;
                for (Iterator<PDField> iter = acroForm.getFieldIterator(); iter.hasNext(); field = iter.next()) {
                    processField(field);
                    if (iter.hasNext()) {
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

        private void processField(PDField field) throws IOException {
            String fullyQualifiedName = field.getFullyQualifiedName();
            String alternateFieldName = field.getAlternateFieldName();
            String fieldValue = null;
            if (field instanceof PDTextField) {
                fieldValue = field.getValueAsString();
            } else if (field instanceof PDCheckBox) {
                fieldValue = "checkbox";
            }

            outputString.append("\"").append(fieldValue).append("\":{\"fullyQualifiedFieldName\":\"").append(fullyQualifiedName).append("\", ");
            outputString.append("\"alternateFieldName\":\"").append(alternateFieldName == null ? "" : alternateFieldName).append("\", ");
            outputString.append("\"fieldType\":\"").append(field.getClass().getName()).append("\"}\n"); 
        }
    }
}
