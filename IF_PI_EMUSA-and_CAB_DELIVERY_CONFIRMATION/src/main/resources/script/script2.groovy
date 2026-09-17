import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    // Read payload or property to perform validation
    def body = message.getBody(String.class);
    
    if (body.contains("InvalidData")) {
        // This explicitly throws a standard runtime exception
        throw new java.lang.Exception("Duplicated request.");
    }
    
    return message;
}