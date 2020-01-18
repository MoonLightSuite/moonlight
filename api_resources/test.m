clear
initMoonLight
%methodsview('eu.quanticol.moonlight.api.ProvaMatlab')
import eu.quanticol.moonlight.api.*
aa = ProvaMatlab;
%class(aa)
somma = aa.somma(2,2);
%pp = [java.lang.Double(2),java.lang.Double(2)];
sommaArray = aa.somma([2,2]);
identity = aa.identity([2,2]);

