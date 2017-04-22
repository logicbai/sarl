/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2017 the original authors or authors.
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
 */

package io.sarl.maven.docs.markdown;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.arakhne.afc.vmutil.FileSystem;
import org.arakhne.afc.vmutil.URISchemeType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.lib.IntegerRange;

import io.sarl.maven.docs.parser.AbstractMarkerLanguageParser;
import io.sarl.maven.docs.parser.DynamicValidationComponent;
import io.sarl.maven.docs.parser.DynamicValidationContext;
import io.sarl.maven.docs.parser.SarlDocumentationParser;
import io.sarl.maven.docs.parser.SectionNumber;

/** Markdown parser.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.6
 */
public class MarkdownParser extends AbstractMarkerLanguageParser {

	/** List of the filename extensions that corresponds to Markdown files.
	 */
	public static final String[] MARKDOWN_FILE_EXTENSIONS = new String[] {
		".md", ".markdown", ".mdown", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		".mkdn", ".mkd", ".mdwn", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		".mdtxt", ".mdtext", //$NON-NLS-1$//$NON-NLS-2$
	};

	/** Default level at which the titles may appear in the outline.
	 */
	public static final int DEFAULT_OUTLINE_TOP_LEVEL = 2;

	/** Indicates if the sections should be numbered by default.
	 */
	public static final boolean DEFAULT_SECTION_NUMBERING = true;

	/** Indicates the default name of the style for the outline.
	 */
	public static final String DEFAULT_OUTLINE_STYLE_ID = "page_outline"; //$NON-NLS-1$

	/** The default format, compatible with {@link MessageFormat} for the section titles.
	 */
	public static final String DEFAULT_SECTION_TITLE_FORMAT = "{0}{1}. {2}"; //$NON-NLS-1$

	/** The default format, compatible with {@link MessageFormat} for the outline entry without auto-numbering.
	 */
	public static final String DEFAULT_OUTLINE_ENTRY_WO_AUTONUMBERING = "{0} [{1}](#{2})"; //$NON-NLS-1$

	/** The default format, compatible with {@link MessageFormat} for the outline entry with auto-numbering.
	 */
	public static final String DEFAULT_OUTLINE_ENTRY_W_AUTONUMBERING = "{0} [{1}. {2}](#{3})"; //$NON-NLS-1$

	private static final String SECTION_PATTERN_AUTONUMBERING =
			"^([#]+)\\s*([0-9]+(?:\\.[0-9]+)*\\.?)?\\s*(.*?)\\s*(?:\\{\\s*([a-z\\-]+)\\s*\\})?\\s*$"; //$NON-NLS-1$

	private static final String SECTION_PATTERN_NO_AUTONUMBERING =
			"^([#]+)\\s*(.*?)\\s*(?:\\{\\s*([a-z\\-]+)\\s*\\})?\\s*$"; //$NON-NLS-1$

	private IntegerRange outlineDepthRange = new IntegerRange(DEFAULT_OUTLINE_TOP_LEVEL, DEFAULT_OUTLINE_TOP_LEVEL);

	private boolean sectionNumbering = DEFAULT_SECTION_NUMBERING;

	private String sectionTitleFormat = DEFAULT_SECTION_TITLE_FORMAT;

	private String sectionNumberFormat = SectionNumber.DEFAULT_SECTION_NUMBER_FORMAT;

	private String outlineEntryWithNumberFormat = DEFAULT_OUTLINE_ENTRY_W_AUTONUMBERING;

	private String outlineEntryWithoutNumberFormat = DEFAULT_OUTLINE_ENTRY_WO_AUTONUMBERING;

	private String outlineStyleId = DEFAULT_OUTLINE_STYLE_ID;

	private boolean localFileReferenceValidation = true;

	private boolean remoteReferenceValidation = true;

	private boolean localImageReferenceValidation = true;

	private boolean transformMdToHtmlReferences = true;

	@Override
	@Inject
	public void setDocumentParser(SarlDocumentationParser parser) {
		super.setDocumentParser(parser);
		updateBlockFormatter();
	}

	@Override
	public void setGithubExtensionEnable(boolean enable) {
		super.setGithubExtensionEnable(enable);
		updateBlockFormatter();
	}

	private void updateBlockFormatter() {
		if (isGithubExtensionEnable()) {
			getDocumentParser().setBlockCodeTemplate((languageName, content) -> {
				return "```" + Strings.emptyIfNull(languageName).toLowerCase() + "\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ Pattern.compile("^", Pattern.MULTILINE).matcher(content).replaceAll("\t") //$NON-NLS-1$ //$NON-NLS-2$
						+ "```\n"; //$NON-NLS-1$
			});
		} else {
			getDocumentParser().setBlockCodeTemplate((languageName, content) -> {
				return Pattern.compile("^", Pattern.MULTILINE).matcher(content).replaceAll("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			});
		}
	}

	@Override
	public String extractPageTitle(String content) {
		final Pattern sectionPattern = Pattern.compile(
				isAutoSectionNumbering() ? SECTION_PATTERN_AUTONUMBERING : SECTION_PATTERN_NO_AUTONUMBERING,
						Pattern.MULTILINE);
		final Matcher matcher = sectionPattern.matcher(content);
		final IntegerRange depthRange = getOutlineDepthRange();
		final int titleGroupId;
		if (isAutoSectionNumbering()) {
			titleGroupId = 3;
		} else {
			titleGroupId = 2;
		}
		while (matcher.find()) {
			final String prefix = matcher.group(1);
			final int clevel = prefix.length();
			if (clevel < depthRange.getStart()) {
				final String title = matcher.group(titleGroupId);
				if (!Strings.isEmpty(title)) {
					return title;
				}
			}
		}
		return null;
	}

	/** Replies the style identifier that should be used for rendering the outline.
	 *
	 * <p>If an identifier exists, the outline will be enclosing by an HTML div tag with
	 * the class and id attributes set to the value.
	 *
	 * @return the outline style identifier.
	 */
	public String getOutlineStyleId() {
		return this.outlineStyleId;
	}

	/** Change the style identifier that should be used for rendering the outline.
	 *
	 * <p>If an identifier exists, the outline will be enclosing by an HTML div tag with
	 * the class and id attributes set to the value.
	 *
	 * @param id the outline style identifier.
	 */
	public void setOutlineStyleId(String id) {
		this.outlineStyleId = id;
	}

	/** Replies if the references to the Markdown files should be transform to references to HTML pages.
	 *
	 * @return {@code true} if the references should be validated.
	 */
	public boolean isMarkdownToHtmlReferenceTransformation() {
		return this.transformMdToHtmlReferences;
	}

	/** Change the flag that indicates if the references the Markdown files should be transform to references to HTML pages.
	 *
	 * @param transform {@code true} if the references should be validated.
	 */
	public void setMarkdownToHtmlReferenceTransformation(boolean transform) {
		this.transformMdToHtmlReferences = transform;
	}

	/** Replies if the references to the local files should be validated.
	 *
	 * @return {@code true} if the references to the local files should be validated.
	 */
	public boolean isLocalFileReferenceValidation() {
		return this.localFileReferenceValidation;
	}

	/** Change the flag that indicates if the references to the local files should be validated.
	 *
	 * @param validate {@code true} if the references to the local files should be validated.
	 */
	public void setLocalFileReferenceValidation(boolean validate) {
		this.localFileReferenceValidation = validate;
	}

	/** Replies if the references to the remote Internet pages should be validated.
	 *
	 * @return {@code true} if the references to the local files should be validated.
	 */
	public boolean isRemoteReferenceValidation() {
		return this.remoteReferenceValidation;
	}

	/** Change the flag that indicates if the references to the remote Internet pages should be validated.
	 *
	 * @param validate {@code true} if the references to the remote Internet pages should be validated.
	 */
	public void setRemoteReferenceValidation(boolean validate) {
		this.remoteReferenceValidation = validate;
	}

	/** Replies if the references to the local images should be validated.
	 *
	 * @return {@code true} if the references to the local images should be validated.
	 */
	public boolean isLocalImageReferenceValidation() {
		return this.localImageReferenceValidation;
	}

	/** Change the flag that indicates if the references to the local images should be validated.
	 *
	 * @param validate {@code true} if the references to the local images should be validated.
	 */
	public void setLocalImageReferenceValidation(boolean validate) {
		this.localImageReferenceValidation = validate;
	}

	/** Change the formats to be applied to the outline entries.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}.
	 *
	 * <p>If section auto-numbering is on,
	 * the first parameter <code>{0}</code> equals to the prefix,
	 * the second parameter <code>{1}</code> equals to the string representation of the section number,
	 * the third parameter <code>{2}</code> equals to the title text, and the fourth parameter
	 * <code>{3}</code> is the reference id of the section.
	 *
	 * <p>If section auto-numbering is off,
	 * the first parameter <code>{0}</code> equals to the prefix,
	 * the second parameter <code>{1}</code> equals to the title text, and the third parameter
	 * <code>{2}</code> is the reference id of the section.
	 *
	 * @param formatWithoutNumbers the format for the outline entries without section numbers.
	 * @param formatWithNumbers the format for the outline entries with section numbers.
	 */
	public void setOutlineEntryFormat(String formatWithoutNumbers, String formatWithNumbers) {
		if (!Strings.isEmpty(formatWithoutNumbers)) {
			this.outlineEntryWithoutNumberFormat = formatWithoutNumbers;
		}
		if (!Strings.isEmpty(formatWithNumbers)) {
			this.outlineEntryWithNumberFormat = formatWithNumbers;
		}
	}

	/** Replies the format to be applied to the outline entries.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}.
	 *
	 * <p>If section auto-numbering is on,
	 * the first parameter <code>{0}</code> equals to the prefix,
	 * the second parameter <code>{1}</code> equals to the string representation of the section number,
	 * the third parameter <code>{2}</code> equals to the title text, and the fourth parameter
	 * <code>{3}</code> is the reference id of the section.
	 *
	 * <p>If section auto-numbering is off,
	 * the first parameter <code>{0}</code> equals to the prefix,
	 * the second parameter <code>{1}</code> equals to the title text, and the third parameter
	 * <code>{2}</code> is the reference id of the section.
	 *
	 * @return the format.
	 */
	public String getOutlineEntryFormat() {
		return isAutoSectionNumbering() ? this.outlineEntryWithNumberFormat : this.outlineEntryWithoutNumberFormat;
	}

	/** Change the format to be applied to the section titles.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}, with
	 * the first parameter <code>{0}</code> equals to the Markdown prefix,
	 * the second parameter <code>{1}</code> equals to the string representation of the section number,
	 * the third parameter <code>{2}</code> equals to the title text, and the fourth parameter
	 * <code>{3}</code> is the identifier of the section.
	 *
	 * @param format the format.
	 */
	public void setSectionTitleFormat(String format) {
		if (!Strings.isEmpty(format)) {
			this.sectionTitleFormat = format;
		}
	}

	/** Replies the format to be applied to the section titles.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}, with
	 * the first parameter <code>{0}</code> equals to the string representation of the section number,
	 * the second parameter <code>{1}</code> equals to the string representation of the section number,
	 * the third parameter <code>{2}</code> equals to the title text, and the fourth parameter
	 * <code>{3}</code> is the identifier of the section.
	 *
	 * @return the format.
	 */
	public String getSectionTitleFormat() {
		return this.sectionTitleFormat;
	}

	/** Change the format to be applied to the section numbers.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}, with
	 * the first parameter <code>{0}</code> equals to the first part of the full section number, and
	 * the second parameter <code>{1}</code> equals to a single section number.
	 *
	 * @param format the format.
	 */
	public void setSectionNumberFormat(String format) {
		if (!Strings.isEmpty(format)) {
			this.sectionNumberFormat = format;
		}
	}

	/** Replies the format to be applied to the section numbers.
	 *
	 * <p>The format must be compatible with {@link MessageFormat}, with
	 * the first parameter <code>{0}</code> equals to the first part of the full section number, and
	 * the second parameter <code>{1}</code> equals to a single section number.
	 *
	 * @return the format.
	 */
	public String getSectionNumberFormat() {
		return this.sectionNumberFormat;
	}

	/** Change the level at which the titles may appear in the outline.
	 *
	 * @param level the level, at least 1.
	 */
	public void setOutlineDepthRange(IntegerRange level) {
		if (level == null) {
			this.outlineDepthRange = new IntegerRange(DEFAULT_OUTLINE_TOP_LEVEL, DEFAULT_OUTLINE_TOP_LEVEL);
		} else {
			this.outlineDepthRange = level;
		}
	}

	/** Replies the level at which the titles may appear in the outline.
	 *
	 * @return the level, at least 1.
	 */
	public IntegerRange getOutlineDepthRange() {
		return this.outlineDepthRange;
	}

	/** Set if the sections are automatically numbered.
	 *
	 * @param enable {@code true} if the section are automatically numbered.
	 */
	public void setAutoSectionNumbering(boolean enable) {
		this.sectionNumbering = enable;
	}

	/** Replies if the sections are automatically numbered.
	 *
	 * @return {@code true} if the section are automatically numbered.
	 */
	public boolean isAutoSectionNumbering() {
		return this.sectionNumbering;
	}

	@Override
	protected String postProcessingTransformation(String content) {
		String result = updateOutline(content);
		result = transformLinks(result);
		return result;
	}

	/** Apply link transformation.
	 *
	 * @param content the original content.
	 * @return the result of the transformation.
	 */
	protected String transformLinks(String content) {
		if (!isMarkdownToHtmlReferenceTransformation()) {
			return content;
		}
		final MutableDataSet options = new MutableDataSet();
		final Parser parser = Parser.builder(options).build();
		final Node document = parser.parse(content);

		final Map<BasedSequence, String> replacements = new TreeMap<>((cmp1, cmp2) -> {
			final int cmp = Integer.compare(cmp2.getStartOffset(), cmp1.getStartOffset());
			if (cmp != 0) {
				return cmp;
			}
			return Integer.compare(cmp2.getEndOffset(), cmp1.getEndOffset());
		});

		final NodeVisitor visitor = new NodeVisitor(
				new VisitHandler<>(Link.class, (it) -> {
					final URL url = FileSystem.convertStringToURL(it.getUrl().toString(), true);
					if (URISchemeType.FILE.isURL(url)) {
						File filename = FileSystem.convertURLToFile(url);
						final String extension = FileSystem.extension(filename);
						if (isMarkdownFileExtension(extension)) {
							filename = FileSystem.replaceExtension(filename, ".html"); //$NON-NLS-1$
							replacements.put(it.getUrl(), filename.toString());
						}
					}
				}));
		visitor.visitChildren(document);

		if (!replacements.isEmpty()) {
			final StringBuilder buffer = new StringBuilder(content);
			for (final Entry<BasedSequence, String> entry : replacements.entrySet()) {
				final BasedSequence seq = entry.getKey();
				buffer.replace(seq.getStartOffset(), seq.getEndOffset(), entry.getValue());
			}
			return buffer.toString();
		}
		return content;
	}

	/** Replies if the given extension is for Markdown file.
	 *
	 * @param extension the extension to test.
	 * @return {@code true} if the extension is for a Markdown file.
	 */
	public static boolean isMarkdownFileExtension(String extension) {
		for (final String ext : MARKDOWN_FILE_EXTENSIONS) {
			if (Strings.equal(ext, extension)) {
				return true;
			}
		}
		return false;
	}

	/** Update the outline tags.
	 *
	 * @param content the content with outline tag.
	 * @return the content with expended outline.
	 */
	@SuppressWarnings({"checkstyle:npathcomplexity", "checkstyle:cyclomaticcomplexity"})
	protected String updateOutline(String content) {
		final Pattern sectionPattern = Pattern.compile(
				isAutoSectionNumbering() ? SECTION_PATTERN_AUTONUMBERING : SECTION_PATTERN_NO_AUTONUMBERING,
						Pattern.MULTILINE);
		final Matcher matcher = sectionPattern.matcher(content);

		final Set<String> identifiers = new TreeSet<>();
		final StringBuilder outline = new StringBuilder();
		outline.append("\n"); //$NON-NLS-1$
		final String outlineStyleId = getOutlineStyleId();
		final boolean styledOutline = !Strings.isEmpty(outlineStyleId);
		if (styledOutline) {
			outline.append("<ul class=\"").append(Strings.convertToJavaString(outlineStyleId)); //$NON-NLS-1$
			outline.append("\" id=\"").append(Strings.convertToJavaString(outlineStyleId)); //$NON-NLS-1$
			outline.append("\">\n\n"); //$NON-NLS-1$
		}
		final IntegerRange outlineDepthRange = getOutlineDepthRange();

		final StringBuffer output;
		final SectionNumber sections;
		final int titleGroupId;
		if (isAutoSectionNumbering()) {
			output = new StringBuffer();
			sections = new SectionNumber();
			titleGroupId = 3;
		} else {
			output = null;
			sections = null;
			titleGroupId = 2;
		}

		int prevLevel = 0;
		int nbOpened = 0;

		while (matcher.find()) {
			final String prefix = matcher.group(1);
			final int clevel = prefix.length();
			if (outlineDepthRange.contains(clevel)) {
				final int relLevel = clevel - outlineDepthRange.getStart();
				final String title = matcher.group(titleGroupId);
				String sectionId = matcher.group(titleGroupId + 1);

				if (output != null) {
					assert sections != null;

					String sectionNumber = matcher.group(2);
					if (!Strings.isEmpty(sectionNumber)) {
						sections.setFromString(sectionNumber, relLevel + 1);
					} else {
						sections.increment(relLevel + 1);
					}
					sectionNumber = formatSectionNumber(sections);

					if (Strings.isEmpty(sectionId)) {
						sectionId = computeHeaderId(
								isAutoSectionNumbering() ? sectionNumber : null,
								title);
						if (!identifiers.add(sectionId)) {
							int idNum = 1;
							String nbId = sectionId + "-" + idNum; //$NON-NLS-1$
							while (!identifiers.add(nbId)) {
								++idNum;
								nbId = sectionId + "-" + idNum; //$NON-NLS-1$
							}
							sectionId = nbId;
						}
					}

					matcher.appendReplacement(output, formatSectionTitle(prefix, sectionNumber, title, sectionId));

					if (styledOutline && (relLevel > 0 || prevLevel > 0) && relLevel != prevLevel) {
						if (relLevel > prevLevel) {
							for (int i = prevLevel; i < relLevel; ++i) {
								outline.append("<ul>\n"); //$NON-NLS-1$
								++nbOpened;
							}
						} else {
							for (int i = relLevel; i < prevLevel; ++i) {
								outline.append("</ul>\n"); //$NON-NLS-1$
								--nbOpened;
							}
						}
					}

					addOutlineEntry(outline, relLevel + 1, sectionNumber, title, sectionId, styledOutline);
				} else {
					addOutlineEntry(outline, relLevel + 1, null, title, sectionId, styledOutline);
				}
				prevLevel = relLevel;
			}
		}

		final String newContent;
		if (output != null) {
			matcher.appendTail(output);
			newContent = output.toString();
		} else {
			newContent = content;
		}

		outline.append("\n"); //$NON-NLS-1$
		if (styledOutline) {
			for (int i = 0; i <= nbOpened; ++i) {
				outline.append("</ul>\n"); //$NON-NLS-1$
			}
		}

		final String outlineTag = getDocumentParser().getOutlineOutputTag();
		return newContent.replaceAll(Pattern.quote(outlineTag), outline.toString());
	}

	/** Create the id of a section header.
	 *
	 * <p>The ID format follows the ReadCarpet standards.
	 *
	 * @param headerNumber the number of the header, or {@code null}.
	 * @param headerText the section header text.
	 * @return the identifier.
	 */
	public static String computeHeaderId(String headerNumber, String headerText) {
		final String fullText = Strings.emptyIfNull(headerNumber) + " " + Strings.emptyIfNull(headerText); //$NON-NLS-1$
		String id = fullText.replaceAll("[^a-zA-Z0-9]+", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		id = id.toLowerCase();
		id = id.replaceFirst("^[^a-zA-Z0-9]+", ""); //$NON-NLS-1$ //$NON-NLS-2$
		id = id.replaceFirst("[^a-zA-Z0-9]+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (Strings.isEmpty(id)) {
			return "section"; //$NON-NLS-1$
		}
		return id;
	}

	/** Update the outline entry.
	 *
	 * @param outline the outline.
	 * @param level the depth level in the outline.
	 * @param sectionNumber the auto-computed section number, or {@code null} if no auto-computed number.
	 * @param title the title of the section.
	 * @param sectionId the identifier of the section.
	 * @param htmlOutput indicates if the output should be HTML or not.
	 */
	protected void addOutlineEntry(StringBuilder outline, int level, String sectionNumber, String title,
			String sectionId, boolean htmlOutput) {
		if (htmlOutput) {
			indent(outline, level - 1, "  "); //$NON-NLS-1$
			outline.append("<li><a href=\"#"); //$NON-NLS-1$
			outline.append(sectionId);
			outline.append("\">"); //$NON-NLS-1$
			if (isAutoSectionNumbering() && !Strings.isEmpty(sectionNumber)) {
				outline.append(sectionNumber).append(". "); //$NON-NLS-1$
			}
			outline.append(title);
			outline.append("</a></li>"); //$NON-NLS-1$
		} else {
			final String prefix = "*"; //$NON-NLS-1$
			final String entry;
			outline.append("> "); //$NON-NLS-1$
			indent(outline, level - 1, "\t"); //$NON-NLS-1$
			if (isAutoSectionNumbering()) {
				entry = MessageFormat.format(getOutlineEntryFormat(), prefix,
						Strings.emptyIfNull(sectionNumber), title, sectionId);
			} else {
				entry = MessageFormat.format(getOutlineEntryFormat(), prefix, title, sectionId);
			}
			outline.append(entry);
		}
		outline.append("\n"); //$NON-NLS-1$
	}

	/** Format the section numbers.
	 *
	 * @param numbers the section numbers, level per level.
	 * @return the formatted section number.
	 */
	protected String formatSectionNumber(SectionNumber numbers) {
		return numbers.toString(getSectionNumberFormat());
	}

	/** Format the section title.
	 *
	 * @param prefix the Markdown prefix.
	 * @param sectionNumber the section number.
	 * @param title the section title.
	 * @param sectionId the identifier of the section.
	 * @return the formatted section title.
	 */
	protected String formatSectionTitle(String prefix, String sectionNumber, String title, String sectionId) {
		return MessageFormat.format(getSectionTitleFormat(), prefix, sectionNumber, title, sectionId) + "\n"; //$NON-NLS-1$
	}

	/** Create indentation in the given buffer.
	 *
	 * @param buffer the buffer.
	 * @param number the number of identations.
	 * @param character the string for a single indentation.
	 */
	protected static void indent(StringBuilder buffer, int number, String character) {
		for (int i = 0; i < number; ++i) {
			buffer.append(character);
		}
	}

	@Override
	protected List<DynamicValidationComponent> getSpecificValidationComponents(String text, File inputFile,
			File rootFolder,
			DynamicValidationContext context) {
		final MutableDataSet options = new MutableDataSet();
		final Parser parser = Parser.builder(options).build();
		final Node document = parser.parse(text);
		final List<DynamicValidationComponent> validators = new ArrayList<>();
		File cfile;
		try {
			cfile = FileSystem.makeRelative(inputFile, rootFolder);
		} catch (IOException exception) {
			cfile = inputFile.getParentFile();
		}
		final File currentFile = cfile;
		final NodeVisitor visitor = new NodeVisitor(
				new VisitHandler<>(Link.class, (it) -> {
					final Iterable<DynamicValidationComponent> components = createValidatorComponents(it,
							currentFile, context);
					for (final DynamicValidationComponent component : components) {
						validators.add(component);
					}
				}),
				new VisitHandler<>(Image.class, (it) -> {
					final Iterable<DynamicValidationComponent> components = createValidatorComponents(it,
							currentFile, context);
					for (final DynamicValidationComponent component : components) {
						validators.add(component);
					}
				}));
		visitor.visitChildren(document);
		return validators;
	}

	/** Compute the number of lines for reaching the given node.
	 *
	 * @param node the node.
	 * @return the line number for the node.
	 */
	protected static int computeLineNo(Node node) {
		final int offset = node.getStartOffset();
		final BasedSequence seq = node.getDocument().getChars();
		int tmpOffset = seq.endOfLine(0);
		int lineno = 1;
		while (tmpOffset < offset) {
			++lineno;
			tmpOffset = seq.endOfLineAnyEOL(tmpOffset + seq.eolLength(tmpOffset));
		}
		return lineno;
	}

	/** Create a validation component for an image reference.
	 *
	 * @param it the image reference.
	 * @param currentFile the current file.
	 * @param context the validation context.
	 * @return the validation components.
	 */
	protected Iterable<DynamicValidationComponent> createValidatorComponents(Image it, File currentFile,
			DynamicValidationContext context) {
		final Collection<DynamicValidationComponent> components = new ArrayList<>();
		if (isLocalImageReferenceValidation()) {
			final int lineno = computeLineNo(it);
			final URL url = FileSystem.convertStringToURL(it.getUrl().toString(), true);
			if (URISchemeType.FILE.isURL(url)) {
				final DynamicValidationComponent component = createLocalImageValidatorComponent(
						it, url, lineno, currentFile, context);
				if (component != null) {
					components.add(component);
				}
			}
		}
		return components;
	}

	/** Create a validation component for an hyper reference.
	 *
	 * @param it the hyper reference.
	 * @param currentFile the current file.
	 * @param context the validation context.
	 * @return the validation components.
	 */
	protected Iterable<DynamicValidationComponent> createValidatorComponents(Link it, File currentFile,
			DynamicValidationContext context) {
		final Collection<DynamicValidationComponent> components = new ArrayList<>();
		if (isLocalFileReferenceValidation() || isRemoteReferenceValidation()) {
			final int lineno = computeLineNo(it);
			final URL url = FileSystem.convertStringToURL(it.getUrl().toString(), true);
			if (URISchemeType.FILE.isURL(url)) {
				if (isLocalFileReferenceValidation()) {
					final DynamicValidationComponent component = createLocalFileValidatorComponent(
							it, url, lineno, currentFile, context);
					if (component != null) {
						components.add(component);
					}
				}
			} else if (URISchemeType.HTTP.isURL(url) || URISchemeType.HTTPS.isURL(url) || URISchemeType.FTP.isURL(url)) {
				if (isRemoteReferenceValidation()) {
					final DynamicValidationComponent component = createRemoteReferenceValidatorComponent(
							it, url, lineno, currentFile, context);
					if (component != null) {
						components.add(component);
					}
				}
			}
		}
		return components;
	}

	/** Create a validation component for a reference to a local image.
	 *
	 * @param it the reference.
	 * @param url the parsed URL of the link.
	 * @param lineno the position of the link into the Markdown file.
	 * @param currentFile the current file.
	 * @param context the validation context.
	 * @return the validation component.
	 */
	@SuppressWarnings("static-method")
	protected DynamicValidationComponent createLocalImageValidatorComponent(Image it, URL url, int lineno,
			File currentFile, DynamicValidationContext context) {
		File fn = FileSystem.convertURLToFile(url);
		if (!fn.isAbsolute()) {
			fn = FileSystem.join(currentFile.getParentFile(), fn);
		}
		final File filename = fn;
		return new DynamicValidationComponent() {
			@Override
			public String functionName() {
				return "Image_reference_test_" + lineno + "_"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public void generateValidationCode(ITreeAppendable it) {
				context.appendFileExistencyTest(it, filename, Messages.MarkdownParser_0);
			}
		};
	}

	/** Create a validation component for an hyper reference to a local file.
	 *
	 * @param it the hyper reference.
	 * @param url the parsed URL of the link.
	 * @param lineno the position of the link into the Markdown file.
	 * @param currentFile the current File.
	 * @param context the validation context.
	 * @return the validation component.
	 */
	@SuppressWarnings("static-method")
	protected DynamicValidationComponent createLocalFileValidatorComponent(Link it, URL url, int lineno,
			File currentFile, DynamicValidationContext context) {
		File fn = FileSystem.convertURLToFile(url);
		if (Strings.isEmpty(fn.getName())) {
			// Special case: the URL should point to a anchor in the current document.
			final String linkRef = url.getRef();
			if (Strings.isEmpty(linkRef)) {
				return new DynamicValidationComponent() {
					@Override
					public String functionName() {
						return "File_reference_test_" + lineno + "_"; //$NON-NLS-1$ //$NON-NLS-2$
					}

					@Override
					public void generateValidationCode(ITreeAppendable it2) {
						it2.append(Assert.class).append(".fail(\"Invalid reference format: "); //$NON-NLS-1$
						it2.append(Strings.convertToJavaString(it.getUrl().toString()));
						it2.append("\");"); //$NON-NLS-1$
					}
				};
			}
			// No need to validate the current file's existency.
			return null;
		}
		if (!fn.isAbsolute()) {
			fn = FileSystem.join(currentFile.getParentFile(), fn);
		}
		final File filename = fn;
		final String extension = FileSystem.extension(filename);
		if (isMarkdownFileExtension(extension) || isHtmlFileExtension(extension)) {
			// Special case: the file may be a HTML or a Markdown file.
			return new DynamicValidationComponent() {
				@Override
				public String functionName() {
					return "Documentation_reference_test_" + lineno + "_"; //$NON-NLS-1$ //$NON-NLS-2$
				}

				@Override
				public void generateValidationCode(ITreeAppendable it) {
					context.setTempResourceRoots(context.getSourceRoots());
					context.appendFileExistencyTest(it, filename, Messages.MarkdownParser_1,
							Iterables.concat(
									Arrays.asList(MARKDOWN_FILE_EXTENSIONS),
									Arrays.asList(HTML_FILE_EXTENSIONS)));
				}
			};
		}
		return new DynamicValidationComponent() {
			@Override
			public String functionName() {
				return "File_reference_test_" + lineno + "_"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public void generateValidationCode(ITreeAppendable it) {
				context.appendFileExistencyTest(it, filename, Messages.MarkdownParser_1);
			}
		};
	}

	/** Create a validation component for an hyper reference to a remote Internet page.
	 *
	 * @param it the hyper reference.
	 * @param url the parsed URL of the link.
	 * @param lineno the position of the link into the Markdown file.
	 * @param currentFile the current file.
	 * @param context the validation context.
	 * @return the validation component.
	 */
	@SuppressWarnings("static-method")
	protected DynamicValidationComponent createRemoteReferenceValidatorComponent(Link it, URL url, int lineno,
			File currentFile, DynamicValidationContext context) {
		return new DynamicValidationComponent() {
			@Override
			public String functionName() {
				return "Web_reference_test_" + lineno + "_"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public void generateValidationCode(ITreeAppendable it) {
				it.append("assertURLAccessibility(").append(Integer.toString(lineno)); //$NON-NLS-1$
				it.append(", new "); //$NON-NLS-1$
				it.append(URL.class).append("(\""); //$NON-NLS-1$
				it.append(Strings.convertToJavaString(url.toExternalForm()));
				it.append("\"));"); //$NON-NLS-1$
			}
		};
	}

}
