package org.grep4j.core.result;

import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.fixtures.ProfileFixtures.localAllProfiles;
import static org.grep4j.core.fixtures.ProfileFixtures.localProfile;
import static org.grep4j.core.fixtures.ProfileFixtures.localProfileWithWildecard;
import static org.grep4j.core.fluent.Dictionary.on;
import static org.grep4j.core.options.Option.extraLinesAfter;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.testng.annotations.Test;

@Test
public class WhenPrintingGrepResults {

	public void aGrepResultsSetWithSingleFile() {
		assertThat(grep(constantExpression("ERROR 1"), on(localProfile()), extraLinesAfter(20)).filterBy(constantExpression("Marco")).toString(),
				is("customer Marco(id=12345) has been updated successfully\n"));

	}

	public void aGrepResultsSetWithMultipleFiles() {
		assertThat(grep(constantExpression("ERROR 1"), on(localProfileWithWildecard("*")), extraLinesAfter(20)).filterBy(constantExpression("Marco"))
				.toString(), is("customer Marco(id=12345) has been updated successfully\n"));

	}

	
        public void aGrepResultsSetWithAllFiles() {
		assertThat(grep(constantExpression("ERROR 1"), on(localAllProfiles()), extraLinesAfter(20)).filterBy(constantExpression("Marco"))
				.toString(),
				is("customer Marco(id=12345) has been updated successfully\ncustomer Marco(id=12345) has been updated successfully\n"));

	}

}
