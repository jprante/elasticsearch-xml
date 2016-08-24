# Elasticsearch XML Plugin

The XML plugin for Elasticsearch is a simple REST filter for sending and receiving XML.

It converts REST HTTP bodies from JSON to XML. It is hoped to be useful to embed Elasticsearch in XML environments.

For sending XML, you must add a HTTP header `Content-type: application/xml`

For receiving XML, you must add a HTTP header `Accept: application/xml`

Each JSON name is converted to a valid XML element name according to ISO 9075.

Because XML is more restrictive than JSON, do not assume that XML can server as a full replacement for JSON in Elasticsearch.

The JSON to XML conversion uses some tricks. Therefore you must not be surprised by edge cases where XML give peculiar results.

## Versions

| Elasticsearch version    | Plugin     | Release date |
| ------------------------ | -----------| -------------|
| 2.3.5                    | 2.3.5.1    | Aug 24, 2016 |
| 2.3.5                    | 2.3.5.0    | Aug 13, 2016 |
| 1.6.0                    | 1.6.0.2    | Jul  3, 2015 |
| 1.4.2                    | 1.4.2.0    | Feb  2, 2015 |
| 1.3.2                    | 1.3.0.0    | Aug 19, 2014 |
| 1.2.2                    | 1.2.2.1    | Jul 22, 2014 |

## Installation

### Elasticsearch 2.x

    ./bin/plugin install 'http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-xml/2.3.5.1/elasticsearch-xml-2.3.5.1-plugin.zip'

### Elasticsearch 1.x

    ./bin/plugin --install xml --url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-xml/1.6.0.2/elasticsearch-xml-1.6.0.2-plugin.zip

Do not forget to restart the node after installing.

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-xml)


# Examples

Consider the following JSON documents.

Command:

    curl '0:9200/_search?pretty'

Output:

    {
      "took" : 2,
      "timed_out" : false,
      "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
      },
      "hits" : {
        "total" : 7,
        "max_score" : 1.0,
        "hits" : [ {
          "_index" : "a",
          "_type" : "b",
          "_id" : "3",
          "_score" : 1.0, "_source" : {"name":"Jörg"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "2",
          "_score" : 1.0, "_source" : {"es:foo":"bar"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "7",
          "_score" : 1.0, "_source" : {"":"Hello World"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "c",
          "_score" : 1.0, "_source" : {"Hello":"World"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "6",
          "_score" : 1.0, "_source" : {"@context":{"p":"http://another.org"},"p:foo":"bar"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "4",
          "_score" : 1.0, "_source" : {"@context":{"p":"http://example.org"},"p:foo":"bar"}
        }, {
          "_index" : "a",
          "_type" : "b",
          "_id" : "5",
          "_score" : 1.0, "_source" : {"@context":{"p":"http://dummy.org"},"p:foo":"bar"}
        } ]
      }
    }

The same in XML.

Command:

    curl -H 'Accept: application/xml'  '0:9200/_search?pretty'
    
Output:    
    
    <root xmlns="http://elasticsearch.org/ns/1.0/" xmlns:p="http://dummy.org">
      <took>3</took>
      <timed_out>false</timed_out>
      <shards>
        <total>5</total>
        <successful>5</successful>
        <failed>0</failed>
      </shards>
      <hits>
        <total>7</total>
        <max_score>1.0</max_score>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>3</id>
          <score>1.0</score>
          <source>
            <name>Jörg</name>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>2</id>
          <score>1.0</score>
          <source>
            <foo>bar</foo>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>7</id>
          <score>1.0</score>
          <source>
            <>Hello World</>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>c</id>
          <score>1.0</score>
          <source>
            <Hello>World</Hello>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>6</id>
          <score>1.0</score>
          <source>
            <context es:p="http://another.org"/>
            <wstxns1:foo xmlns:wstxns1="http://another.org">bar</wstxns1:foo>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>4</id>
          <score>1.0</score>
          <source>
            <context es:p="http://example.org"/>
            <wstxns2:foo xmlns:wstxns2="http://example.org">bar</wstxns2:foo>
          </source>
        </hits>
        <hits>
          <index>a</index>
          <type>b</type>
          <id>5</id>
          <score>1.0</score>
          <source>
            <context es:p="http://dummy.org"/>
            <p:foo>bar</p:foo>
          </source>
        </hits>
      </hits>

As shown above, with the `@context` name in JSON, you can declare XML namespaces.

The `@context` is similar to JSON-LD's `@context` but not that powerful.

## XML Attributes

If JSON names are used with a `@` as starting letter, they will appear as XML attribute.

If XML attributes are passed in sending documents, they will appear as normal JSON names.

If nested XML do not lead to a proper JSON object, an empty JSON name is used, which might not be useful.

Command:

    curl -XPOST -H 'Accept: application/xml' '0:9200/a/c/1' -d '<root><name attr="test">value</name></root>'
    curl '0:9200/a/c/1?pretty'

Output:

    {
      "_index" : "a",
      "_type" : "c",
      "_id" : "1",
      "_version" : 1,
      "found" : true, "_source" : {"name":{"attr":"test","":"value"}}
    }

Another example.

Command:

    curl -XPOST '0:9200/a/c/2' -d '{"test":{"@attr": "value"}}'
    curl -H 'Accept: application/xml' '0:9200/a/c/2?pretty'

Output:

    <root xmlns="http://elasticsearch.org/ns/1.0/" xmlns:es="http://elasticsearch.org/ns/1.0/">
      <index>a</index>
      <type>c</type>
      <id>2</id>
      <version>1</version>
      <found>true</found>
      <source>
        <test es:attr="value"/>
      </source>
    </root>


# License

Elasticsearch XML Plugin

Copyright (C) 2014 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

