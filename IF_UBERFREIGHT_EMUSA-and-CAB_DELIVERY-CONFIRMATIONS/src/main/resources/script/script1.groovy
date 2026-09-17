import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
/*
 * Maps the modeExecution payload to the flat Delivery structure.
 *
 * Delivery     <- modeExecution.freights[].references[name == "DELIVERY NUMBER"].value
 *                 (falls back to name == "PRIMARY REFERENCE" if not found)
 * FinDlvDate   <- stops[sequenceNumber == 99].apptEarliestDatetimeUTC
 */
Message processData(Message message) {
    String body = message.getBody(String) ?: ''
    message.setBody(transform(body))
    message.setHeader('Content-Type', 'application/json')
    return message
}
static String transform(String rawJson) {
    def root = parse(rawJson)
    def me = root?.modeExecution ?: [:]
    List stops = (me.stops ?: []) as List
    def stopBySeq = { int seq -> stops.find { toInt(it?.sequenceNumber) == seq } }
    def lastStop = stopBySeq(99) ?: (stops ? stops.last() : null)
    List allRefs = (me.freights ?: []).collectMany { (it?.references ?: []) as List }
    def delivery = findRef(allRefs, 'DELIVERY NUMBER') ?: findRef(allRefs, 'PRIMARY REFERENCE')
    def target = [
            Delivery    : str(delivery),
            FinDlvDate  : ts(lastStop?.apptEarliestDatetimeUTC),
    ]
    return JsonOutput.prettyPrint(JsonOutput.toJson(target))
}
/* ---------- helpers ---------- */
static Object parse(String raw) {
    def slurper = new JsonSlurper()
    String s = raw?.trim() ?: '{}'
    try {
        return slurper.parseText(s)
    } catch (ignored) {
        // tolerate the double-wrapped "{ { ... } }" form
        if (s.startsWith('{') && s.endsWith('}')) {
            return slurper.parseText(s.substring(1, s.length() - 1).trim())
        }
        throw ignored
    }
}
/** Finds the value of the first reference whose name matches (case-insensitive, trimmed). */
static Object findRef(List refs, String name) {
    refs.find { name.equalsIgnoreCase(it?.name?.toString()?.trim()) }?.value
}
static String str(Object v) {
    (v == null || 'null' == v.toString()) ? '' : v.toString().trim()
}
static Integer toInt(Object v) {
    try { v == null ? null : Integer.valueOf(v.toString().trim()) } catch (ignored) { null }
}
/** "2025-12-16 16:01" -> "2025-12-16T16:01:00" (seconds added when missing). */
static String ts(Object v) {
    String s = str(v)
    if (!s) return ''
    s = s.replace(' ', 'T')
    def m = (s =~ /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})(?::(\d{2}))?/)
    return m.find() ? "${m.group(1)}T${m.group(2)}:${m.group(3) ?: '00'}" : s
}