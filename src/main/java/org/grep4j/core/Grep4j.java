package org.grep4j.core;

import static org.grep4j.core.task.ForkController.maxGrepTaskThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.grep4j.core.model.Profile;
import org.grep4j.core.options.Option;
import org.grep4j.core.options.Options;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;
import org.grep4j.core.task.GrepRequest;
import org.grep4j.core.task.GrepTask;

import com.google.common.collect.ImmutableList;

/**
 * Entry Class for the Grep4j API. Usage example:
 * 
 * <pre>
 * import static org.grep4j.core.Grep4j.grep;
 * import static org.grep4j.core.fluent.Dictionary.on;
 * ...
 * 
 * List<Profile> profiles = Arrays.asList(aProfile,moreProfiles);
 * 
 * 
 * GrepResultsSet results = grep("USER(12435)", on(profiles)));
 * System.out.println("Total occurrences found : " + results.totalOccurrences());
 * 
 * for (GrepResult singleResult : results) {			
 * 		System.out.println(singleResult);
 * }
 * </pre>
 * 
 * Grep4j used for test:
 * <pre>
 * import static org.grep4j.core.Grep4j.grep;
 * import static org.grep4j.core.fluent.Dictionary.on;
 * import static org.grep4j.core.fluent.Dictionary.executing;
 * ...
 * 
 * List<Profile> profiles = Arrays.asList(aProfile,moreProfiles);
 * assertThat(executing(grep("USER(12435)", on(profiles))).totalOccurrences(), is(1));
 * </pre>
 * 
 * <p>
 * 
 * Reference: http://code.google.com/p/grep4j/
 * 
 * @author Marco Castigliego
 * @author Giovanni Gargiulo
 */
public final class Grep4j {

	private final String expression;
	private final ImmutableList<Profile> profiles;
	private final Options options;
	private final GrepResults results;
	private final List<GrepRequest> grepRequests;
	private final boolean isRegexExpression;

	/**
	 * Creates an instance of Grep4j that accepts also extra lines options.
	 * 
	 * It also protects profiles and extra lines options with ImmutableList.
	 * 
	 * @param expression
	 * @param profiles
	 * @param options
	 * @param isRegexExpression
	 */
	private Grep4j(String expression, List<Profile> profiles, List<Option> options, boolean isRegexExpression) {
		this.grepRequests = new ArrayList<GrepRequest>();
		this.results = new GrepResults();
		this.expression = expression;
		this.profiles = ImmutableList.copyOf(profiles);
		this.isRegexExpression = isRegexExpression;
		this.options = new Options(options);
	}

	/**
	 * Creates an instance of Grep4j
	 * 
	 * It also protects profiles with ImmutableList.
	 * 
	 * @param expression
	 * @param profiles
	 * @param isRegexExpression
	 */
	private Grep4j(String expression, List<Profile> profiles, boolean isRegexExpression) {
		this.grepRequests = new ArrayList<GrepRequest>();
		this.results = new GrepResults();
		this.expression = expression;
		this.profiles = ImmutableList.copyOf(profiles);
		this.options = new Options();
		this.isRegexExpression = isRegexExpression;
	}

	/**
	 * 
	 * This utility method executes the grep command and return the {@link GrepResults}
	 * containing the result of the grep 
	 * 
	 * Example:
	 * <pre>
	 * import static org.grep4j.core.Grep4j.grep;
	 * import static org.grep4j.core.fluent.Dictionary.on;
	 * ...
	 * 
	 * grep("expression", on(profiles()));
	 * </pre>
	 * 
	 * 
	 * @param expression
	 * @param profiles
	 * @return GlobalGrepResult
	 */
	public static GrepResults grep(GrepExpression grepExpression, List<Profile> profiles) {
		return new Grep4j(grepExpression.getText(), profiles, grepExpression.isRegularExpression()).execute().andGetResults();
	}

	/**
	* 
	* This utility method executes the grep command and return the {@link GrepResults}
	* containing the result of the grep
	* 
	* It also protects the List of profiles and Options wrapping them into an
	* ImmutableList.
	* Example of ExtraLines is :
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesAfter;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("expression", on(profiles()), extraLinesAfter(100));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* System.out.println("Total occurrences found : " + results.totalOccurrences("another expression within 100 lines after"));
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 
	* </pre>
	* or
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesBefore;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("eXpReSsIoN", on(profiles()), withOption("-i"));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 
	* </pre>	 
	* or
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesBefore;
	* import static org.grep4j.core.Grep4j.extraLinesAfter;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("expression", on(profiles()), extraLinesBefore(100), extraLinesAfter(100), withOption("-o"));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 	* </pre>
	* 
	* @param expression
	* @param profiles
	* @param option
	* @return GlobalGrepResult
	*/
	public static GrepResults grep(GrepExpression grepExpression, List<Profile> profiles, Option... options) {
		return new Grep4j(grepExpression.getText(), profiles, Arrays.asList(options), grepExpression.isRegularExpression()).execute()
				.andGetResults();
	}

	/**
	 * 
	 * This utility method executes the grep command and return the {@link GrepResults}
	 * containing the result of the grep 
	 * 
	 * Example:
	 * <pre>
	 * import static org.grep4j.core.Grep4j.grep;
	 * import static org.grep4j.core.fluent.Dictionary.on;
	 * ...
	 * 
	 * grep("expression", on(profile));
	 * </pre>
	 * 
	 * 
	 * @param expression
	 * @param profile
	 * @return GlobalGrepResult
	 */
	public static GrepResults grep(GrepExpression grepExpression, Profile profile) {
		return new Grep4j(grepExpression.getText(), Collections.singletonList(profile), grepExpression.isRegularExpression()).execute()
				.andGetResults();
	}

	/**
	* 
	* This utility method executes the grep command and return the {@link GrepResults}
	* containing the result of the grep
	* 
	* It also protects the List of profiles and Options wrapping them into an
	* ImmutableList.
	* Example of ExtraLines is :
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesAfter;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("expression", on(profiles()), extraLinesAfter(100));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* System.out.println("Total occurrences found : " + results.totalOccurrences("another expression within 100 lines after"));
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 
	* </pre>
	* or
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesBefore;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("expression", on(profiles()), extraLinesBefore(100));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* System.out.println("Total occurrences found : " + results.totalOccurrences("another expression within 100 lines before"));
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 
	* </pre>	 
	* or
	* <pre>
	* import static org.grep4j.core.Grep4j.grep;
	* import static org.grep4j.core.Grep4j.extraLinesBefore;
	* import static org.grep4j.core.Grep4j.extraLinesAfter;
	* import static org.grep4j.core.fluent.Dictionary.on;
	* ...
	* 
	* GrepResultsSet results = grep("expression", on(profile), extraLinesBefore(100), extraLinesAfter(100));
	* System.out.println("Total occurrences found : " + results.totalOccurrences());
	* System.out.println("Total occurrences found : " + results.totalOccurrences("another expression within 100 lines before and 100 after"));
	* 
	* for (GrepResult singleResult : results) {			
	* 		System.out.println(singleResult);
	* } 	* </pre>
	* 
	* @param expression
	* @param profile
	* @param options
	* @return GlobalGrepResult
	*/
	public static GrepResults grep(GrepExpression grepExpression, Profile profile, Option... options) {
		return new Grep4j(grepExpression.getText(), Collections.singletonList(profile), Arrays.asList(options), grepExpression.isRegularExpression())
				.execute().andGetResults();
	}

	/**
	 * Regular expression
	 * 
	 * @param text
	 * @return GrepExpression
	 */
	public static GrepExpression regularExpression(String text) {
		return new GrepExpression(text, true);
	}

	/**
	 * Regular Language(string)
	 * 
	 * @param text
	 * @return GrepExpression
	 */
	public static GrepExpression regularLanguage(String text) {
		return new GrepExpression(text, false);
	}

	/**
	 * This method will:
	 * <ol>
	 * <li>Clean all result and request lists</li>
	 * <li>Verify the input checking that mandatory fields have been correctly
	 * populated</li>
	 * <li>Prepare {@link GrepRequest}s to be executed, based on the inputs
	 * passed</li>
	 * <li>Execute {@link GrepRequest} for each valid {@link Profile}</li>
	 * </ol>
	 */
	private Grep4j execute() {
		clean();
		verifyInputs();
		prepareCommandRequests();
		executeCommands();
		return this;
	}

	private void clean() {
		grepRequests.clear();
		results.clear();
	}

	/**
	 * @return a {@link GrepResults}s
	 */
	private GrepResults andGetResults() {
		return results;
	}

	private void verifyInputs() {
		if (expression == null || expression.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"No expression to grep was specified");
		}
		if (profiles == null || profiles.isEmpty()) {
			throw new IllegalArgumentException(
					"No profile to grep was specified");
		} else {
			for (Profile profile : profiles) {
				profile.validate();
			}
		}
	}

	private void executeCommands() {
		ExecutorService executorService = null;
		CompletionService<List<GrepResult>> completionService = null;
		try {		    
			executorService = Executors.newFixedThreadPool(maxGrepTaskThreads(this.options, grepRequests.size()));
			completionService = new ExecutorCompletionService<List<GrepResult>>(executorService);
			for (GrepRequest grepRequest : grepRequests) {
				completionService.submit(new GrepTask(grepRequest));
			}

			for (@SuppressWarnings("unused")
			GrepRequest grepRequest : grepRequests) {
				for (GrepResult singleGrepResult : completionService.take().get()) {
					results.add(singleGrepResult);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when executing the GrepTask", e);
		} finally {
			if (executorService != null) {
				executorService.shutdownNow();
			}
		}
	}

	private void prepareCommandRequests() {
		for (Profile profile : profiles) {
			GrepRequest grepRequest = new GrepRequest(expression, profile, isRegexExpression);
			grepRequest.addOptions(options);
			
			grepRequests.add(grepRequest);
		}
	}

}
