import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    // 1. Retrieve the property 'Delivery'
    def map = message.getProperties();
    def deliveryNumber = map.get("Delivery");
    
    // 2. Fallback text if the property happens to be empty
    if (deliveryNumber == null || deliveryNumber.toString().trim().isEmpty()) {
        deliveryNumber = "[Unknown Delivery]";
    }
    
    // 3. Construct the dynamic error message
    String errorMessage = "Duplicate request. This delivery (${deliveryNumber}) has already been processed.";
    
    // 4. Throw the explicit exception to halt the iFlow
    throw new java.lang.Exception(errorMessage);
}