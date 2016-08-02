/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package opennlp.tools.parse_thicket.kernel_interface;


import java.util.ArrayList;
import java.util.List;

import opennlp.tools.parse_thicket.ParseThicket;
import opennlp.tools.parse_thicket.VerbNetProcessor;
import opennlp.tools.parse_thicket.external_rst.MatcherExternalRST;
import opennlp.tools.parse_thicket.external_rst.ParseThicketWithDiscourseTree;

/*
 * This class performs TK learning based on parse thicket which includes RST relations only 
 * based on Surdeanu at al RST parser. It does sentence parsing and NLP pipeline of 
 * Surdeanu's wrapper of Stanford NLP
 */
public class TreeKernelBasedClassifierOfDiscourseTree extends TreeKernelBasedClassifierMultiplePara{

	private MatcherExternalRST matcherRST = new MatcherExternalRST();

	protected List<String> formTreeKernelStructuresMultiplePara(List<String> texts, String flag) {
		//TODO
		//this.setShortRun();	
		List<String> extendedTreesDumpTotal = new ArrayList<String>();
		try {

			for(String text: texts){
				// get the parses from original documents, and form the training dataset
				try {
					System.out.print("About to build pt with external rst from "+text + "\n...");
					ParseThicket pt = matcherRST.buildParseThicketFromTextWithRST(text);
					if (pt == null)
						continue;
					System.out.print("About to build extended forest with external rst...");
					List<String> extendedTreesDump =  // use direct option (true
							buildReptresentationForDiscourseTreeAndExtensions((ParseThicketWithDiscourseTree)pt, true);
					for(String line: extendedTreesDump)
						extendedTreesDumpTotal.add(flag + " |BT| "+line + " |ET| ");
					System.out.println("DONE");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return extendedTreesDumpTotal;
	}

	private List<String> buildReptresentationForDiscourseTreeAndExtensions(ParseThicketWithDiscourseTree pt, boolean bDirectDT){
		List<String> extendedTreesDump = new ArrayList<String>();
		if (!bDirectDT)
			// option 1: use RST relation for extended trees 
			extendedTreesDump = treeExtender.buildForestForRSTArcs(pt);
		else {
			// option 2: use DT directly
			extendedTreesDump.add(pt.getDtDump());
		    extendedTreesDump.add(pt.getDtDumpWithPOS());
		    extendedTreesDump.add(pt.getDtDumpWithEmbeddedTrees());
		    extendedTreesDump.add(pt.getDtDumpWithVerbNet());
		}		
		return extendedTreesDump;
	}
	
	/*
	 * dtDump
	 * 1 |BT| (elaboration (joint (attribution (I though) (I d tell you a little about what I like to write )) (joint (And I like to immerse myself in my topics ) (joint (I just like to dive right i) (and become sort of a human guinea pig )))) (elaboration (joint (And I see my life as a series of experiments ) (joint (So , I work for Esquire magazine ) (elaboration (elaboration (and a couple of years ago I wrote an articl) (called My Outsourced Life )) (enablement (where I hired a team of people in Bangalore , India ) (to live my life for me ))))) (elaboration (So they answered my emails ) (They answered my phone )))) |ET|
	 * 
	 * getDtDumpWithPOS
	 * 
	 *  1 |BT| (elaboration (joint (attribution (I PRP)(thought VBD) (I PRP)(d NN)(tell VBP)(you PRP)(a DT)(little JJ)(about IN)(what WP)(I PRP)(like VBP)(to TO)(write VB)) (joint (And CC)(I PRP)(like VBP)(to TO)(immerse VB)(myself PRP)(in IN)(my PRP$)(topics NNS) (joint (I PRP)(just RB)(like VBP)(to TO)(dive NN)(right NN)(in IN) (and CC)(become VB)(sort NN)(of IN)(a DT)(human JJ)(guinea NN)(pig NN)))) (elaboration (joint (And CC)(I PRP)(see VBP)(my PRP$)(life NN)(as IN)(a DT)(series NN)(of IN)(experiments NNS) (joint (So RB)(I PRP)(work VBP)(for IN)(Esquire NNP)(magazine NN) (elaboration (elaboration (and CC)(a DT)(couple NN)(of IN)(years NNS)(ago IN)(I PRP)(wrote VBD)(an DT)(article NN) (called VBN)(My PRP$)(Outsourced JJ)(Life NNP)) (enablement (where WRB)(I PRP)(hired VBD)(a DT)(team NN)(of IN)(people NNS)(in IN)(Bangalore NNP)(India NNP) (to TO)(live VB)(my PRP$)(life NN)(for IN)(me PRP))))) (elaboration (So IN)(they PRP)(answered VBD)(my PRP$)(emails NNS) (They PRP)(answered VBD)(my PRP$)(phone NN)))) |ET| 
	 * 
	 * getDtDumpWithEmbeddedTrees()
	 * 1 |BT| (elaboration (joint (attribution (SBAR (S (NP (PRP I)) (VP (ADVP (NN d)) (VBP tell) (NP (PRP you)) (PP (NP (DT a) (JJ little)) (IN about) (SBAR (WHNP (WP what)) (S (NP (PRP I)) (VP (VBP like) (S (VP (TO to) (VP (VB write))))))))))) (VBP tell)) (joint (VP (VBP like) (S (VP (TO to) (VP (VB immerse) (NP (PRP myself)) (PP (IN in) (NP (PRP$ my) (NNS topics))))))) (joint (VP (VP (VBP like) (PP (TO to) (NP (NN dive) (NN right))) (PP (IN in))) (CC and) (VP (VB become) (NP (NP (NN sort)) (PP (IN of) (NP (DT a) (JJ human) (NN guinea) (NN pig)))))) (NP (NP (NN sort)) (PP (IN of) (NP (DT a) (JJ human) (NN guinea) (NN pig))))))) (elaboration (joint (VP (VBP see) (NP (PRP$ my) (NN life)) (PP (IN as) (NP (NP (DT a) (NN series)) (PP (IN of) (NP (NNS experiments)))))) (joint (S (NP (PRP I)) (VP (VBP work) (PP (IN for) (NP (NNP Esquire) (NN magazine))))) (elaboration (elaboration (NN couple) (JJ Outsourced)) (enablement (VP (VBP work) (PP (IN for) (NP (NNP Esquire) (NN magazine)))) (NP (PRP$ my) (NN life)))))) (elaboration (VP (VBD answered) (NP (PRP$ my) (NNS emails))) (NP (PRP$ my) (NN phone))))) |ET|
	 
	 pt.getDtDumpWithVerbNet()
	 1 |BT| (elaboration (joint (attribution (I PRP)(thought VBD) (I PRP)(d NN) (tell  (tell-372 tell-372 tell-372 ) (NP V NP NP V NP PP-topic NP V NP S ) (NP NP-PPof-PP NP-S ) ) (you PRP)(a DT)(little JJ)(about IN)(what WP)(I PRP)(like VBP)(to TO)(write VB)) (joint (And CC)(I PRP)(like VBP)(to TO)(immerse VB)(myself PRP)(in IN)(my PRP$)(topics NNS) (joint (I PRP)(just RB)(like VBP)(to TO)(dive NN)(right NN)(in IN) (and CC)(become VB)(sort NN)(of IN)(a DT)(human JJ)(guinea NN)(pig NN)))) (elaboration (joint (And CC)(I PRP) (see  (see-301 see-301 see-301 ) (NP V NP NP V that S NP V NP-ATTR-POS PP-oblique NP V how S NP V what S ) (Basic Transitive S Attribute Object Possessor-Attribute Factoring Alternation HOW-S WHAT-S ) ) (my PRP$)(life NN)(as IN)(a DT)(series NN)(of IN)(experiments NNS) (joint (So RB)(I PRP)(work VBP)(for IN)(Esquire NNP)(magazine NN) (elaboration (elaboration (and CC)(a DT)(couple NN)(of IN)(years NNS)(ago IN)(I PRP)(wrote VBD)(an DT)(article NN) (call  (dub-293 dub-293 dub-293 ) (NP V NP NP NP V NP ) (NP-NP Basic Transitive ) ) (My PRP$)(Outsourced JJ)(Life NNP)) (enablement (where WRB)(I PRP) (hire  (hire-1353 hire-1353 hire-1353 ) (NP V NP NP V NP PP-predicate ) (NP NP-PPas-PP ) ) (a DT)(team NN)(of IN)(people NNS)(in IN)(Bangalore NNP)(India NNP) (to TO)(live VB)(my PRP$)(life NN)(for IN)(me PRP))))) (elaboration (So IN)(they PRP)(answered VBD)(my PRP$)(emails NNS) (They PRP)(answered VBD)(my PRP$)(phone NN)))) |ET|
	 *
	 */
	
	
	
	public static void main(String[] args){
		VerbNetProcessor p = VerbNetProcessor.
				getInstance("/Users/borisgalitsky/Documents/workspace/relevance-based-on-parse-trees/src/test/resources"); 

		TreeKernelBasedClassifierOfDiscourseTree proc = new TreeKernelBasedClassifierOfDiscourseTree();
		proc.setKernelPath("/Users/borisgalitsky/Documents/workspace/relevance-based-on-parse-trees/src/test/resources/tree_kernel/");
		proc.trainClassifier(
				"/Users/borisgalitsky/Documents/workspace/relevance-based-on-parse-trees/src/test/resources/style_recognizer/txt/ted",
				"/Users/borisgalitsky/Documents/workspace/relevance-based-on-parse-trees/src/test/resources/style_recognizer/txt/Tedi");
	}

}
/*
 * 
RST - based run
Number of examples: 6980, linear space size: 10
ted vs Tedi

estimating ...
Setting default regularization parameter C=1.0000
Optimizing............................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 1931
done. (3597 iterations)
Optimization finished (78 misclassified, maxdiff=0.00100).
Runtime in cpu-seconds: 198.37
Number of SV: 3830 (including 652 at upper bound)
L1 loss: loss=261.78883
Norm of weight vector: |w|=41.37067
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=1712.53247
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.05
XiAlpha-estimate of the error: error<=11.53% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>97.01% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>89.47% (rho=1.00,depth=0)
Number of kernel evaluations: 73092240

GENERAL RUN (the same set of texts)
Number of examples: 21146, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0000
Optimizing.........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 8849
done. (5770 iterations)
Optimization finished (231 misclassified, maxdiff=0.00098).
Runtime in cpu-seconds: 1486.33
Number of SV: 5368 (including 940 at upper bound)
L1 loss: loss=582.99311
Norm of weight vector: |w|=46.91885
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=2202.37876
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.13
XiAlpha-estimate of the error: error<=5.57% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>98.42% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>95.18% (rho=1.00,depth=0)
Number of kernel evaluations: 550748695
Writing model file...done


Number of examples: 7461, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0000
Optimizing............................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 2091
done. (3773 iterations)
Optimization finished (87 misclassified, maxdiff=0.00096).
Runtime in cpu-seconds: 231.42
Number of SV: 4092 (including 680 at upper bound)
L1 loss: loss=280.03696
Norm of weight vector: |w|=42.82963
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=1835.37688
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.05
XiAlpha-estimate of the error: error<=11.54% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>96.75% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>89.59% (rho=1.00,depth=0)
Number of kernel evaluations: 94432306
Writing model file...done



SMALL SET

Number of examples: 172, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0000
Optimizing.......................................................done. (56 iterations)
Optimization finished (0 misclassified, maxdiff=0.00076).
Runtime in cpu-seconds: 0.01
Number of SV: 172 (including 59 at upper bound)
L1 loss: loss=7.38525
Norm of weight vector: |w|=12.46777
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=156.44537
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.00
XiAlpha-estimate of the error: error<=44.77% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>79.55% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>54.26% (rho=1.00,depth=0)
Number of kernel evaluations: 20139
Writing model file...done


LONGER RUN, DTs only
Number of examples: 720, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0000
Optimizing............................................................................................................................................................................................................................................................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 114
done. (269 iterations)
Optimization finished (11 misclassified, maxdiff=0.00096).
Runtime in cpu-seconds: 0.17
Number of SV: 712 (including 140 at upper bound)
L1 loss: loss=117.83422
Norm of weight vector: |w|=12.73402
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=163.15526
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.00
XiAlpha-estimate of the error: error<=20.14% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>99.14% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>80.42% (rho=1.00,depth=0)
Number of kernel evaluations: 283615
Writing model file...done

HYBRID RUN
Number of examples: 8301, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0000
Optimizing................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 2323
done. (4206 iterations)
Optimization finished (98 misclassified, maxdiff=0.00099).
Runtime in cpu-seconds: 299.94
Number of SV: 4870 (including 846 at upper bound)
L1 loss: loss=398.61389
Norm of weight vector: |w|=44.95124
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=2021.61414
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.05
XiAlpha-estimate of the error: error<=12.32% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>97.15% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>88.53% (rho=1.00,depth=0)
Number of kernel evaluations: 138447398
Writing model file...done

HYBRID FULL RUN

Number of examples: 2880, linear space size: 10

estimating ...
Setting default regularization parameter C=1.0021
Optimizing...........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................
 Checking optimality of inactive variables...done.
 Number of inactive variables = 1035
done. (1820 iterations)
Optimization finished (162 misclassified, maxdiff=0.00099).
Runtime in cpu-seconds: 1.35
Number of SV: 1552 (including 556 at upper bound)
L1 loss: loss=426.90789
Norm of weight vector: |w|=25.52139
Norm of longest example vector: |x|=1.00000
Estimated VCdim of classifier: VCdim<=652.34149
Computing XiAlpha-estimates...done
Runtime for XiAlpha-estimates in cpu-seconds: 0.01
XiAlpha-estimate of the error: error<=23.92% (rho=1.00,depth=0)
XiAlpha-estimate of the recall: recall=>92.67% (rho=1.00,depth=0)
XiAlpha-estimate of the precision: precision=>80.55% (rho=1.00,depth=0)
Number of kernel evaluations: 4075095
Writing model file...done
 */