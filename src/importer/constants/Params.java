/*
 * This file is part of Compare.
 *
 *  Compare is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Compare is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Compare.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */
package importer.constants;

/**
 * Parameters passed from the GUI to us
 * @author desmond
 */
public class Params 
{
    /** mvd version+groups for version 1 */
    public final static String DOCID = "docid";
    /** mvd version+groups for version 1 */
    public final static String VERSION1 = "version1";
    /** mvd version+groups for version 1 */
    public final static String VERSION2 = "version2";
    /** kind of differences to generate */
    public final static String DIFF_KIND = "diff_kind";
    /** passed-in form param base name for corcodes */
    public final static String CORCODE = "CORCODE";
    /** passed-in form param base name for styles */
    public final static String STYLE = "STYLE";
    /** set of selected versions if not ALL */
    public final static String SELECTED_VERSIONS = "SELECTED_VERSIONS";
    /** name of list dropdowns etc */
    public final static String NAME = "name";
    /** ID of long name string (to facilitate dynamic replacement */
    public final static String LONG_NAME_ID = "long_name_id";
    /** prefix for short version (value=long version name)*/
    public final static String SHORT_VERSION = "VERSION_";
    /** config parameter */
    public final static String LC_STYLE = "style";
    /** indicate that this is just for demo purposes */
    public final static String DEMO = "demo";
    public final static String TITLE = "title";
    /** chosen plain text filter  */
    public final static String FILTER = "FILTER";
    /** chosen splitter config  */
    public final static String SPLITTER = "splitter";
    /** chosen stripper config  */
    public final static String STRIPPER = "stripper";
    public final static String SIMILARITY = "SIMILARITY";
    /** chosen plain text filter config  */
    public final static String TEXT = "text";
    /** encoding of uploaded data */
    public final static String ENCODING = "encoding";
    public final static String DICT = "dict";
    public final static String HH_EXCEPTIONS = "hh_exceptions";
    public final static String COLLECTION = "collection";
    public final static String CORFORM = "corform";
}
