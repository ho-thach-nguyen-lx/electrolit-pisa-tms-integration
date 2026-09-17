import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonParserType

def Message processData(Message message) {
    def body = message.getBody(String.class)
    
    def slurper = new JsonSlurper().setType(JsonParserType.LAX)
    def json = slurper.parseText(body)
    
    def outboundDelivery = json.data.OutboundDelivery
    
    message.setProperty("OutboundDelivery", outboundDelivery)
    
    return message
}