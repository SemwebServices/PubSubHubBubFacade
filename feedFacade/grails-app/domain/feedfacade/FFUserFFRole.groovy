package feedfacade

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.apache.commons.lang.builder.HashCodeBuilder

@ToString(cache=true, includeNames=true, includePackage=false)
class FFUserFFRole implements Serializable {

	private static final long serialVersionUID = 1

	FFUser FFUser
	FFRole FFRole

	@Override
	boolean equals(other) {
		if (other instanceof FFUserFFRole) {
			other.FFUserId == FFUser?.id && other.FFRoleId == FFRole?.id
		}
	}

	@Override
	int hashCode() {
		def builder = new HashCodeBuilder()
		if (FFUser) builder.append(FFUser.id)
		if (FFRole) builder.append(FFRole.id)
		builder.toHashCode()
	}

	static FFUserFFRole get(long FFUserId, long FFRoleId) {
		criteriaFor(FFUserId, FFRoleId).get()
	}

	static boolean exists(long FFUserId, long FFRoleId) {
		criteriaFor(FFUserId, FFRoleId).count()
	}

	private static DetachedCriteria criteriaFor(long FFUserId, long FFRoleId) {
		FFUserFFRole.where {
			FFUser == FFUser.load(FFUserId) &&
			FFRole == FFRole.load(FFRoleId)
		}
	}

	static FFUserFFRole create(FFUser FFUser, FFRole FFRole) {
		def instance = new FFUserFFRole(FFUser: FFUser, FFRole: FFRole)
		instance.save()
		instance
	}

	static boolean remove(FFUser u, FFRole r) {
		if (u != null && r != null) {
			FFUserFFRole.where { FFUser == u && FFRole == r }.deleteAll()
		}
	}

	static int removeAll(FFUser u) {
		u == null ? 0 : FFUserFFRole.where { FFUser == u }.deleteAll()
	}

	static int removeAll(FFRole r) {
		r == null ? 0 : FFUserFFRole.where { FFRole == r }.deleteAll()
	}

	static constraints = {
		FFRole validator: { FFRole r, FFUserFFRole ur ->
			if (ur.FFUser?.id) {
				FFUserFFRole.withNewSession {
					if (FFUserFFRole.exists(ur.FFUser.id, r.id)) {
						return ['userRole.exists']
					}
				}
			}
		}
	}

	static mapping = {
		id composite: ['FFUser', 'FFRole']
		version false
	}
}
