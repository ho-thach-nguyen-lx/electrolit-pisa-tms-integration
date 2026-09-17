import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonParserType
import groovy.json.JsonOutput

/*
 * Reads the JSON body, strips a trailing 'Z' from FinDlvDate,
 * and returns the updated JSON as the message body.
 *
 * Input:
 * {
 *   "Delivery": "80005994",
 *   "FinDlvDate": "2026-09-03T17:00:00Z"
 * }
 *
 * Output:
 * {
 *   "Delivery": "80005994",
 *   "FinDlvDate": "2026-09-03T17:00:00"
 * }
 */
def Message processData(Message message) {

    def is = message.getBody(java.io.InputStream)

    if (is == null) {
        return message
    }

    def body = is.getText("UTF-8")

    if (body == null || body.trim().isEmpty()) {
        return message
    }

    def parsed = new JsonSlurper().setType(JsonParserType.CHARACTER_SOURCE).parseText(body)
    def json = new LinkedHashMap(parsed)

    if (json?.FinDlvDate != null && json.FinDlvDate.toString().trim().length() > 0) {
        json.FinDlvDate = json.FinDlvDate.toString().trim().replaceAll("Z\$", "")
    }

    def updatedBody = JsonOutput.toJson(json)

    message.setBody(updatedBody)

    return message
}