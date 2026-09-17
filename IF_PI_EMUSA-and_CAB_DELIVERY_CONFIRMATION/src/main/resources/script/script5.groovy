import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap

/*
 * Reads the XML body, extracts the value at the FinDlvDate element/path,
 * strips a trailing 'Z' (if present), wraps it in the OData datetime'...'
 * literal, and sets it as the FinDlvDate property.
 *
 * Adjust the XML parsing section below if FinDlvDate is nested deeper
 * or has a namespace.
 */
def Message processData(Message message) {

    def body = message.getBody(String) as String

    if (body == null || body.trim().isEmpty()) {
        return message
    }

    def xml = new XmlSlurper().parseText(body)

    // Adjust this path to match the actual XML structure.
    // Example assumes FinDlvDate can be found anywhere in the tree.
    def rawValue = xml.'**'.find { it.name() == 'FinDlvDate' }?.text()

    if (rawValue == null || rawValue.trim().isEmpty()) {
        // Element not found or empty - leave FinDlvDate property unset
        return message
    }

    def dtValue = rawValue.trim()

    // Strip trailing Z if present (anchored to end of string)
    dtValue = dtValue.replaceAll("Z\$", "")

    // Build the OData datetime literal
    def finDlvDate = "datetime'" + dtValue + "'"

    message.setProperty("FinDlvDate", finDlvDate)

    return message
}