/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.domain.model;

@SuppressWarnings("rawtypes")
public interface DeeplyMergeable<M extends Mergeable> extends Mergeable<M> {
	
	DeeplyMergeable<M> merge(M objToMerge);
	
}
