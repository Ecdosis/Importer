/*
 * This file is part of Importer.
 *
 *  Importer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Importer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Importer.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */

package importer.constants;

/**
 * Configurable global settings
 * @author desmond
 */
public class Globals 
{
    /** controls local/external fetching of jquery */
    public static final String JQUERY_SITE = "code.jquery.com";
    //public static final String JQUERY_SITE = "localhost";  
    /** disables import */
    public static final boolean DEMO = true;
    /** maximum size of an uploaded file - don't increase unless you have to */
    public static int MAX_UPLOAD_LEN = 102400;
    /** default dictionary */
    public static String DEFAULT_DICT = "en_GB";
}
