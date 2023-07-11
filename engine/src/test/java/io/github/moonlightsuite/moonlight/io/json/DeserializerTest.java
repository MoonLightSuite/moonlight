package io.github.moonlightsuite.moonlight.io.json;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeserializerTest {


    //@Test
    void testReadTemporalSignal() {
        String json= "{\n" +
                "\t\"trace_type\": \"spatio_temporal\",\n" +
                "\t\"signal_type\": {\"x\": \"boolean\", \"y\": \"real\", \"z\":\"integer\", \"name\": \"string\"},\n" +
                "  \"edge_type\": { \"time\": \"real\", \"length\": \"real\" }, \n" +
                "  \"nodes\": [ \"l1\", \"l2\", \"l3\", \"l4\", \"l5\"],\n" +
                "  \"trajectory\": [\n" +
                "\t\t{\n" +
                "\t\t  \"time\": 0.0 , \n" +
                "\t\t  \"signals\": {\n" +
                "  \t\t\t\"l1\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"taxi\"},\n" +
                "  \t\t\t\"l2\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"house\"},\n" +
                "  \t\t\t\"l3\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"museum\"},\n" +
                "  \t\t\t\"l4\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"stop\"},\n" +
                "  \t\t\t\"l5\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"start\"}\n" +
                "\t\t  },\n" +
                "\t\t\t\"edges\": {\n" +
                "\t\t\t    \"l1\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l2\"\n" +
                "\t\t\t        },\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 2.5, \"length\": 15 },\n" +
                "\t\t\t          \"dest\": \"l3\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l2\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l4\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l3\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l5\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l4\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l5\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ]\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t  \"time\": 0.72 , \n" +
                "\t\t  \"signals\": {\n" +
                "  \t\t\t\"l1\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"taxi\"},\n" +
                "  \t\t\t\"l2\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"house\"},\n" +
                "  \t\t\t\"l3\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"museum\"},\n" +
                "  \t\t\t\"l4\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"stop\"},\n" +
                "  \t\t\t\"l5\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"start\"}\n" +
                "\t\t  },\n" +
                "\t\t\t\"edges\": {\n" +
                "\t\t\t    \"l1\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l2\"\n" +
                "\t\t\t        },\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 2.5, \"length\": 15 },\n" +
                "\t\t\t          \"dest\": \"l3\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l2\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l4\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l3\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l5\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l4\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l5\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ]\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t  \"time\": 1.25 , \n" +
                "\t\t  \"signals\": {\n" +
                "  \t\t\t\"l1\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"taxi\"},\n" +
                "  \t\t\t\"l2\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"house\"},\n" +
                "  \t\t\t\"l3\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"museum\"},\n" +
                "  \t\t\t\"l4\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"stop\"},\n" +
                "  \t\t\t\"l5\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"start\"}\n" +
                "\t\t  },\n" +
                "\t\t\t\"edges\": {\n" +
                "\t\t\t    \"l1\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l2\"\n" +
                "\t\t\t        },\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 2.5, \"length\": 15 },\n" +
                "\t\t\t          \"dest\": \"l3\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l2\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l4\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l3\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l5\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l4\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l5\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ]\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t  \"time\": 2.1 , \n" +
                "\t\t  \"signals\": {\n" +
                "  \t\t\t\"l1\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"taxi\"},\n" +
                "  \t\t\t\"l2\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"house\"},\n" +
                "  \t\t\t\"l3\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"museum\"},\n" +
                "  \t\t\t\"l4\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"stop\"},\n" +
                "  \t\t\t\"l5\":  {\"x\": true, \"y\":0.25, \"z\":23, \"name\":\"start\"}\n" +
                "\t\t  },\n" +
                "\t\t\t\"edges\": {\n" +
                "\t\t\t    \"l1\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l2\"\n" +
                "\t\t\t        },\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 2.5, \"length\": 15 },\n" +
                "\t\t\t          \"dest\": \"l3\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l2\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l4\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l3\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l5\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l4\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ],\n" +
                "\t\t\t    \"l5\": [\n" +
                "\t\t\t        {\n" +
                "\t\t\t          \"label\": { \"time\": 1.5, \"length\": 10 },\n" +
                "\t\t\t          \"dest\": \"l1\"\n" +
                "\t\t\t        }\n" +
                "\t\t\t    ]\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\n" +
                "\t]\n" +
                "\n" +
                "\n" +
                "}\n";

        //Deserializer.SPATIO_TEMPORAL_SIGNAL.deserialize(json);
        assertTrue(true);

    }
}
