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
| 1.2.1                    | 1.2.1.0    | Jun 30, 2014 |


## Checksum

| File                                         | SHA1                                     |
| ---------------------------------------------| -----------------------------------------|
| elasticsearch-xml-1.2.1.0-plugin.zip         | 8a176ab66c16bdee322fe6ba75e805ddb271d6c3 |

## Installation

    ./bin/plugin --install xml --url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-xml/1.2.1.0/elasticsearch-xml-1.2.1.0-plugin.zip


Do not forget to restart the node after installing.

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-xml)


# Examples

Consider the following JSON documents

    curl '0:9200/_search?pretty'
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

The same in XML

    curl -H 'Accept: application/xml'  '0:9200/_search?pretty'
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

Example

    curl -XPOST -H 'Content-type: application/xml' '0:9200/a/c/1' -d '<root><name attr="test">value</name></root>'

Result

    curl '0:9200/a/c/1?pretty'
    {
      "_index" : "a",
      "_type" : "c",
      "_id" : "1",
      "_version" : 1,
      "found" : true, "_source" : {"name":{"attr":"test","":"value"}}
    }

Another example

    curl -XPOST '0:9200/a/c/2' -d '{"test":{"@attr": "value"}}'

Result

    curl -H 'Accept: application/xml' '0:9200/a/c/2?pretty'
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
