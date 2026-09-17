import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

Message processData(Message message) {
    java.io.InputStream bodyStream = message.getBody(java.io.InputStream)
    GPathResult xml = new XmlSlurper().parse(bodyStream)
    GPathResult shipment = xml.Shipment

    Map<String, Object> result = [:]
    result.domainId          = asInt(shipment.DomainId.text())
    result.accountId         = asInt(shipment.AccountId.text())
    result.action            = shipment.Action.text()
    result.deliveryNumber    = shipment.DeliveryNumber.text()
    result.equipmentTypeCode = shipment.EquipmentTypeCode.text()
    result.paymentMethodType = shipment.PaymentMethodType.text()
    result.shipmentType      = shipment.ShipmentType.text()

    // ---- Stops (array, unwrap Stops>Stops, Location>Location, Address>Address) ----
    List<Map<String, Object>> stops = []
    shipment.Stops.Stops.each { NodeChild st ->
        GPathResult loc  = st.Location.Location
        GPathResult addr = loc.Address.Address
        stops << ([
            action          : st.Action.text(),
            type            : st.Type.text(),
            sequenceNumber  : asInt(st.SequenceNumber.text()),
            earliestDateTime: st.EarliestDateTime.text(),
            latestDateTime  : st.LatestDateTime.text(),
            location: [
                id     : loc.Id.text(),
                name   : loc.Name.text(),
                address: [
                    address1: addr.Address1.text(),
                    city    : addr.City.text(),
                    state   : addr.State.text(),
                    zip     : addr.Zip.text(),
                    country : addr.Country.text()
                ]
            ]
        ] as Map<String, Object>)
    }
    result.stops = stops

    // ---- Freights (array, unwrap Freights>Freights) ----
    List<Map<String, Object>> freightsList = []
    shipment.Freights.Freights.each { NodeChild fr ->
        Map<String, Object> freight = [:]
        freight.action                  = fr.Action.text()
        freight.originStopSequence      = asInt(fr.OriginStopSequence.text())
        freight.destinationStopSequence = asInt(fr.DestinationStopSequence.text())
        freight.orderType                = fr.OrderType.text()
        freight.hot                      = fr.Hot.text().toBoolean()

        List<Map<String, Object>> lineItems = []
        fr.LineItems.LineItems.each { NodeChild li ->
            lineItems << ([
                action              : li.Action.text(),
                id                  : li.Id.text(),
                productNumber       : li.ProductNumber.text(),
                description         : li.Description.text(),
                freightType         : li.FreightType.text(),
                commodityDescription: li.CommodityDescription.text(),
                ltlClass            : li.LtlClass.text(),
                nmfc                : li.Nmfc.text(),
                quantity            : li.Quantity.text(),
                quantityUom         : li.QuantityUom.text(),
                handlingUnit        : li.HandlingUnit.text(),
                handlingUnitUom     : li.HandlingUnitUom.text(),
                weight              : li.Weight.text(),
                weightUom           : li.WeightUom.text()
            ] as Map<String, Object>)
        }
        freight.lineItems = lineItems

        List<Map<String, Object>> refs = []
        fr.References.References.each { NodeChild r ->
            refs << ([name: r.Name.text(), value: r.Value.text()] as Map<String, Object>)
        }
        freight.references = refs

        List<Map<String, Object>> supportInfos = []
        fr.SupportInfos.SupportInfos.each { NodeChild s ->
            supportInfos << ([name: s.Name.text(), value: s.Value.text()] as Map<String, Object>)
        }
        freight.supportInfos = supportInfos

        freightsList << freight
    }
    result.freights = freightsList

    message.setBody(JsonOutput.toJson(result))
    return message
}

Object asInt(String v) {
    if (v == null) return null
    String trimmed = v.trim()
    return trimmed.isInteger() ? trimmed.toInteger() : trimmed
}