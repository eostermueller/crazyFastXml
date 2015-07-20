package com.github.eostermueller.validate;

import org.apache.jmeter.samplers.SampleResult;


/**
 * @author erikostermueller
 *
 */
public class XmlValidateSampler01  extends XmlValidateSampler02 {
	public XmlValidateSampler01() {
		this.ynUseCache = false;
	}
	public static void main(String args[]) {
		XmlValidateSampler01 sampler = new XmlValidateSampler01();
		sampler.setupTest(null);
		
		SampleResult sampleResult = sampler.runTest(null);
		System.out.println("Success [" + sampleResult.getResponseCode() + "]");
		System.out.println("Result [" + sampleResult.getResponseDataAsString() + "]");
		
	}
	
}
