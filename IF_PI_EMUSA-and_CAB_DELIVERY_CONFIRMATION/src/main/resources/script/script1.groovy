import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    throw new RuntimeException("Marking MPL as Failed intentionally")
}