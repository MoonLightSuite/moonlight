/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018 
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.JSonSignalReader;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.Test;
import sun.misc.ClassLoaderUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;


public class TestFormula {

    @Test
    public void testFormula() {
        //formula
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula aeb= new AndFormula(a,b);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("trace.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            System.out.println(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
            System.out.println();
//            HashMap<String,Function<Parameters,Function<Double,Boolean>>> mappa = new HashMap<>();
//            mappa.put("a", y-> x->x>2);
//            mappa.put("b", y-> x->x<2);
//            TemporalMonitoring<Double,Boolean> monitoring = new TemporalMonitoring(mappa,new DoubleDomain());
//            monitoring.visit(aeb,Boolean);


    }


}


