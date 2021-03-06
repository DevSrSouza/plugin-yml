/*
 * Copyright (c) 2017 Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.pluginyml.nukkit

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.io.Serializable

class NukkitPluginDescription(project: Project) : Serializable {
    var name: String? = null
    var main: String? = null
    var version: String? = null
    var api: List<String>? = null
    var description: String? = null
    var load: PluginLoadOrder? = null
    var author: String? = null
    var authors: List<String>? = null
    var website: String? = null
    var depend: List<String>? = null
    @JsonProperty("softdepend") var softDepend: List<String>? = null
    @JsonProperty("loadbefore") var loadBefore: List<String>? = null
    var prefix: String? = null
    @JsonProperty("default-permission") var defaultPermission: Permission.Default? = null

    // DSL provider for commands and permissions (not serialized)
    @Transient @JsonIgnore
    val commands: NamedDomainObjectContainer<NukkitPluginDescription.Command> = project.container(NukkitPluginDescription.Command::class.java)
    @Transient @JsonIgnore
    val permissions: NamedDomainObjectContainer<NukkitPluginDescription.Permission> = project.container(NukkitPluginDescription.Permission::class.java) {
        Permission(project, it)
    }

    // Java/Jackson serialization for commands and permissions
    internal val commandMap: Map<String, NukkitPluginDescription.Command> = commands.asMap
        @JsonProperty("commands") get() = field.toMap() // Return copy
    internal val permissionMap: Map<String, NukkitPluginDescription.Permission> = permissions.asMap
        @JsonProperty("permissions") get() = field.toMap() // Return copy

    // For Groovy DSL
    fun commands(closure: Closure<Unit>) = commands.configure(closure)
    fun permissions(closure: Closure<Unit>) = permissions.configure(closure)

    enum class PluginLoadOrder {
        STARTUP,
        POSTWORLD
    }

    data class Command(@Transient @JsonIgnore val name: String) : Serializable {
        var description: String? = null
        var aliases: List<String>? = null
        var permission: String? = null
        @JsonProperty("permission-message") var permissionMessage: String? = null
        var usage: String? = null
    }

    data class Permission(@Transient @JsonIgnore val project: Project, @Transient @JsonIgnore val name: String) : Serializable {

        var description: String? = null
        var default: Default? = null

        // DSL provider for recursive children (not serialized)
        @Transient @JsonIgnore
        val children: NamedDomainObjectContainer<NukkitPluginDescription.Permission> = project.container(NukkitPluginDescription.Permission::class.java) {
            Permission(project, it)
        }

        // Java/Jackson serialization for commands and permissions
        internal val childrenMap: Map<String, NukkitPluginDescription.Permission> = children.asMap
            @JsonProperty("children") get() = field.toMap() // Return copy

        // For Groovy DSL
        fun children(closure: Closure<Unit>) = children.configure(closure)

        enum class Default {
            @JsonProperty("true")   TRUE,
            @JsonProperty("false")  FALSE,
            @JsonProperty("op")     OP,
            @JsonProperty("!op")    NOT_OP
        }
    }
}

