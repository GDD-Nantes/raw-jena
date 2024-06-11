
export class FedShopQueryExamples {

    queries = [
        { name: "q01g",
          description: "A consumer is looking for a product and has a general idea about what she wants.",
          query: `PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT DISTINCT ?product ?label WHERE { 
    ?product rdfs:label ?label .
    
    # const bsbm:ProductType464
    ?product rdf:type ?localProductType .
    ?localProductType owl:sameAs bsbm:ProductType464 .

    # const bsbm:ProductFeature32006
    ?product bsbm:productFeature ?localProductFeature1 . 
    ?localProductFeature1 owl:sameAs bsbm:ProductFeature32006 .

    # const bsbm:ProductFeature23377
    ?product bsbm:productFeature ?localProductFeature2 . 
    ?localProductFeature2 owl:sameAs bsbm:ProductFeature23377 .
    ?product bsbm:productPropertyNumeric1 ?value1 . 
    
    # const "775"^^xsd:integer < ?value1
    FILTER (?value1 > "775"^^xsd:integer) 
}
ORDER BY ?product ?label
LIMIT 10
`},
        { name: "q02b",
          description: "The consumer wants to view basic information about products found by Query 1.",
          query: `PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?label ?comment ?producer ?ProductFeatureLabel ?propertyTextual1 ?propertyTextual2 ?propertyTextual3 ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4  WHERE {
    # const bsbm:Product116212
    ?localProduct owl:sameAs bsbm:Product116212 .
    ?localProduct rdfs:label ?label .
    ?localProduct rdfs:comment ?comment .
    ?localProduct bsbm:producer ?p .
    ?p rdfs:label ?producer .
    #?localProduct dc:publisher ?p . 
    ?localProduct bsbm:productFeature ?localProductFeature1 .
    ?localProductFeature1 owl:sameAs ?ProductFeature1 .
    ?localProductFeature1 rdfs:label ?ProductFeatureLabel .
    ?localProduct bsbm:productPropertyTextual1 ?propertyTextual1 .
    ?localProduct bsbm:productPropertyTextual2 ?propertyTextual2 .
    ?localProduct bsbm:productPropertyTextual3 ?propertyTextual3 .
    ?localProduct bsbm:productPropertyNumeric1 ?propertyNumeric1 .
    ?localProduct bsbm:productPropertyNumeric2 ?propertyNumeric2 .
    OPTIONAL { ?localProduct bsbm:productPropertyTextual4 ?propertyTextual4 }
    OPTIONAL { ?localProduct bsbm:productPropertyTextual5 ?propertyTextual5 }
    OPTIONAL { ?localProduct bsbm:productPropertyNumeric4 ?propertyNumeric4 }
}`},
        { name: 'q03j',
          description: 'After looking at information about some products, the consumer has a more specific idea what she wants.<br/>Therefore, she asks for products having several features but not having a specific other feature.',
          query:`PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?product ?label WHERE {
    ?localProduct owl:sameAs ?product .
    ?localProduct rdfs:label ?label .
    # const bsbm:ProductType400
    ?localProduct rdf:type ?localProductType .
    ?localProductType owl:sameAs bsbm:ProductType400 .

    # const bsbm:ProductFeature9164
    ?localProduct bsbm:productFeature ?localProductFeature1 .
    ?localProductFeature1 owl:sameAs bsbm:ProductFeature9164 .
    ?localProduct bsbm:productPropertyNumeric1 ?p1 .
    # const "651"^^xsd:integer < ?p1 
    FILTER ( ?p1 > "651"^^xsd:integer ) 
    ?localProduct bsbm:productPropertyNumeric3 ?p3 .
    # const "1170"^^xsd:integer > ?p3
    FILTER (?p3 < "1170"^^xsd:integer )
    
    OPTIONAL { 
        # const!* not bsbm:ProductFeature12662
        ?localProduct bsbm:productFeature ?localProductFeature2 .
        ?localProductFeature2 owl:sameAs bsbm:ProductFeature12662 .
        ?localProduct rdfs:label ?testVar 
    }
    FILTER (!bound(?testVar)) 
}
ORDER BY ?product ?label
LIMIT 10
`},
        { name: 'q04c',
          description: 'After looking at information about some products, the consumer has a more specific idea what we wants.<br/>Therefore, she asks for products matching either one set of features or another set.',
          query: `PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT DISTINCT ?product ?label ?propertyTextual WHERE {
    { 
        ?product rdfs:label ?label .
        # const!* bsbm:ProductType775
        ?product rdf:type ?localProductType .
        ?localProductType owl:sameAs bsbm:ProductType775 .
        # const!* bsbm:ProductFeature30789
        ?product bsbm:productFeature ?localProductFeature1 .
        ?localProductFeature1 owl:sameAs bsbm:ProductFeature30789.
        # const** bsbm:ProductFeature8048 != bsbm:ProductFeature30789
        ?product bsbm:productFeature ?localProductFeature2 .
        ?localProductFeature2 owl:sameAs bsbm:ProductFeature8048.
        ?product bsbm:productPropertyTextual1 ?propertyTextual .
        ?product bsbm:productPropertyNumeric1 ?p1 .
        # const** "1105.0"^^xsd:double < ?p1
        FILTER ( ?p1 > "1105.0"^^xsd:double )
    } UNION {
        ?product rdfs:label ?label .
        # const!* bsbm:ProductType775
        ?product rdf:type ?localProductType .
        ?localProductType owl:sameAs bsbm:ProductType775 .
        # const!* bsbm:ProductFeature30789
        ?product bsbm:productFeature ?localProductFeature1 .
        ?localProductFeature1 owl:sameAs bsbm:ProductFeature30789 .
        # const* bsbm:ProductFeature22975 != bsbm:ProductFeature8048, bsbm:ProductFeature30789
        ?product bsbm:productFeature ?localProductFeature3 .
        ?localProductFeature3 owl:sameAs bsbm:ProductFeature22975 .
        ?product bsbm:productPropertyTextual1 ?propertyTextual .
        ?product bsbm:productPropertyNumeric2 ?p2 .
        # const "1266.0"^^xsd:double < ?p2
        FILTER ( ?p2 > "1266.0"^^xsd:double ) 
    } 
}
ORDER BY ?product ?label ?propertyTextual
LIMIT 10
`},
        { name: 'q05j',
          description: 'The consumer has found a product that fulfills his requirements.<br/>She now wants to find products with similar features.',
          query: `PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT DISTINCT ?product ?localProductLabel WHERE { 
    ?localProduct rdfs:label ?localProductLabel .
    
    ?localProduct bsbm:productFeature ?localProdFeature .
    ?localProduct bsbm:productPropertyNumeric1 ?simProperty1 .
    ?localProduct bsbm:productPropertyNumeric2 ?simProperty2 .    

    ?localProduct owl:sameAs ?product .
    ?localProdFeature owl:sameAs ?prodFeature .

    ?localProductXYZ bsbm:productFeature ?localProdFeatureXYZ .
    ?localProductXYZ bsbm:productPropertyNumeric1 ?origProperty1 .
    ?localProductXYZ bsbm:productPropertyNumeric2 ?origProperty2 .

    # const bsbm:Product171547
    ?localProductXYZ owl:sameAs bsbm:Product171547 .
    ?localProdFeatureXYZ owl:sameAs ?prodFeature .

    FILTER(bsbm:Product171547 != ?product)
    # Values are pre-determined because we knew the boundaries from the normal distribution
    FILTER(?simProperty1 < (?origProperty1 + 20) && ?simProperty1 > (?origProperty1 - 20))
    FILTER(?simProperty2 < (?origProperty2 + 70) && ?simProperty2 > (?origProperty2 - 70))
}
ORDER BY ?product ?localProductLabel
LIMIT 5
`},
        {name: 'q06d',
         description: 'The consumer remembers parts of a product name from former searches.<br/>She wants to find the product again by searching for the parts of the name that she remembers.',
         query: `PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?localProduct ?label WHERE {
    ?localProduct rdfs:label ?label .
    ?localProduct rdf:type bsbm:Product .
    # const "pyrenees" in ?label 
    FILTER regex(lcase(str(?label)), "pyrenees")
}`},
        {name: 'q07f',
         description: 'The consumer has found a product which fulfills his requirements.<br/>Now she wants in-depth information about this product.<br/>It includes offers from German vendors and product reviews if existent.',
         query: `PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle ?reviewer ?revName ?rating1 ?rating2 WHERE {
    ?localProduct rdf:type bsbm:Product .
    # const bsbm:Product72080
    ?localProduct owl:sameAs bsbm:Product72080 .
    ?localProduct rdfs:label ?productLabel .
    OPTIONAL {
        ?offer bsbm:product ?offerProduct . 
        ?offerProduct  owl:sameAs bsbm:Product72080 .
        ?offer bsbm:price ?price .
        ?offer bsbm:vendor ?vendor .
        ?vendor rdfs:label ?vendorTitle .
        ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#FR> .
        ?offer bsbm:validTo ?date .

        # const "2008-04-25T00:00:00"^^xsd:dateTime < ?date
        FILTER (?date > "2008-04-25T00:00:00"^^xsd:dateTime )
    }
    OPTIONAL {
        ?review bsbm:reviewFor ?reviewProduct .
        ?reviewProduct owl:sameAs bsbm:Product72080 .
        ?review rev:reviewer ?reviewer .
        ?reviewer foaf:name ?revName .
        ?review dc:title ?revTitle .
        OPTIONAL { ?review bsbm:rating1 ?rating1 . }
        OPTIONAL { ?review bsbm:rating2 ?rating2 . }
    }
}`},
        {name: 'q08f',
         description: 'The consumer wants to read the 20 most recent English language reviews about a specific product.',
         query: `PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4  WHERE { 
    # const bsbm:Product97918
    ?review bsbm:reviewFor ?localProductXYZ .
    ?localProductXYZ owl:sameAs bsbm:Product97918 .
    ?review dc:title ?title .
    ?review rev:text ?text .
    FILTER(langMatches( lang(?text), "en" ))
    ?review bsbm:reviewDate ?reviewDate .
    ?review rev:reviewer ?reviewer .
    ?reviewer foaf:name ?reviewerName .
    OPTIONAL { ?review bsbm:rating1 ?rating1 . }
    OPTIONAL { ?review bsbm:rating2 ?rating2 . }
    OPTIONAL { ?review bsbm:rating3 ?rating3 . }
    OPTIONAL { ?review bsbm:rating4 ?rating4 . }
}
ORDER BY ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4
LIMIT 20
`},
        { name: 'q09c',
          description: 'In order to decide whether to trust a review, the consumer asks for any kind of information that is available about the reviewer.',
          query: `
PREFIX rev: <http://purl.org/stuff/rev#>

SELECT ?x WHERE { 
    # const <http://www.ratingsite3.fr/Review1276>
    <http://www.ratingsite3.fr/Review1276> rev:reviewer ?x 
}`},
          { name: 'q10e',
            description: 'The consumer wants to buy from a vendor in the United States that can deliver within 3 days.<br/>She also looks for the cheapest offer that fulfills these requirements.',
            query: `PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT DISTINCT ?offer ?price WHERE {
    # const bsbm:Product87739
    ?offer bsbm:product ?localProductXYZ .
    ?localProductXYZ owl:sameAs bsbm:Product87739 .
    ?offer bsbm:vendor ?vendor .
    #?offer dc:publisher ?vendor .
    ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .
    ?offer bsbm:deliveryDays ?deliveryDays .
    FILTER(?deliveryDays <= 3)
    ?offer bsbm:price ?price .
    ?offer bsbm:validTo ?date .
    # const "2008-04-10T00:00:00"^^xsd:dateTime < ?date
    FILTER (?date > "2008-04-10T00:00:00"^^xsd:dateTime )
}
ORDER BY ?offer ?price
LIMIT 10
`},
          { name: 'q11a',
            description: 'After deciding on a specific offer, the consumer wants to get all information that is directly related to this offer.',
            query : `PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?property ?hasValue ?isValueOf WHERE {
    # const <http://www.vendor6.fr/Offer886>
    <http://www.vendor6.fr/Offer886> bsbm:product ?product . 
    { <http://www.vendor6.fr/Offer886> ?property ?hasValue }
    UNION
    { ?isValueOf ?property <http://www.vendor6.fr/Offer886> }
}`},
        { name: 'q12b',
          description: 'After deciding on a specific offer, the consumer wants to save information about this offer on her machine.',
          query: `PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT * WHERE { 
    # const <http://www.vendor6.fr/Offer846>
    <http://www.vendor6.fr/Offer846> bsbm:product ?productURI .
    ?productURI owl:sameAs ?ProductXYZ . 
    ?productURI rdfs:label ?productlabel .
    <http://www.vendor6.fr/Offer846> bsbm:vendor ?vendorURI .
    ?vendorURI rdfs:label ?vendorname .
    ?vendorURI foaf:homepage ?vendorhomepage .
    <http://www.vendor6.fr/Offer846> bsbm:offerWebpage ?offerURL .
    <http://www.vendor6.fr/Offer846> bsbm:price ?price .
    <http://www.vendor6.fr/Offer846> bsbm:deliveryDays ?deliveryDays .
    <http://www.vendor6.fr/Offer846> bsbm:validTo ?validTo 
}`}
    ]
    
    constructor(dialog, yasqe) {
        const container = document.createElement("span");
        container.setAttribute("class", "examples_dialog_content");
        
        const table = document.createElement("table");
        const tbody = document.createElement("tbody");
        
        this.queries.forEach(q => {
            if (q.query === "") { return; }
            
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            const button = document.createElement("button");
            button.setAttribute("class", "examples_dialog_button")
            button.innerHTML = q.description;
            button.addEventListener("click", () => {
                yasqe.setValue(q.query);
            });
            button.setAttribute("title", q.name + ":\n\n" + q.query);
            
            td.appendChild(button);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });
        
        table.appendChild(tbody);
        container.appendChild(table);
        dialog.appendChild(container);
    }
    
    
}
