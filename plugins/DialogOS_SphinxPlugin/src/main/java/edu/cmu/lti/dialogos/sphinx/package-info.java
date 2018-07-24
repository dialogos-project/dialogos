/**
 * Classes that constitute the Sphinx 4-based speech recognition plugin for DialogOS.
 * The code is structured as follows:
 *
 * edu.cmu.lti.dialogos.sphinx.plugin.Plugin is the class that can be found and loaded as a service through DialogOS.
 * It keeps a reference to Sphinx, registers the SphinxNode and manages language configuration.
 *
 * edu.cmu.lti.dialogos.sphinx.plugin.Settings keeps settings of the Sphinx-Plugin such as:
 *  - default language (if nothing is specified by the node)
 *  - pronunciation exception dictionary
 *
 */
package edu.cmu.lti.dialogos.sphinx;
