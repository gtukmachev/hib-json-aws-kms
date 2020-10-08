package tga.hib_json_aws_kms.model

import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.*

@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
@Entity
class Box(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var login: String? = null,
            info: BoxInfo? = null,
        var oldInfo: OldBoxInfo? = null
) {

        @Type(type = "json") @Column(length = 5000)
        var info: BoxInfo? = info
                get() = when {
                        (field == null && oldInfo != null) -> { field = oldInfo!!.toBoxInfo(); field }
                        else -> field
                }


        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Box) return false

                if (id != other.id) return false
                if (login != other.login) return false
                if (oldInfo != other.oldInfo) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id ?: 0
                result = 31 * result + (login?.hashCode() ?: 0)
                result = 31 * result + (oldInfo?.hashCode() ?: 0)
                return result
        }

        override fun toString(): String {
                return "Box(id=$id, login=$login, info=$info, oldInfo=$oldInfo)"
        }

        fun copy(
                id: Int? = this.id,
                login: String? = this.login,
                info: BoxInfo? = this.info,
                oldInfo: OldBoxInfo? = this.oldInfo
        ): Box {
                return Box(id, login, info, oldInfo)
        }

}


data class BoxInfo(
        var inn: String? = null,
        var address: String? = null,
        var size: Int? = null
)

@Embeddable
data class OldBoxInfo(
        var inn: String? = null,
        var address: String? = null
) {
       public fun toBoxInfo(): BoxInfo = BoxInfo(inn = inn, address = address)
}
