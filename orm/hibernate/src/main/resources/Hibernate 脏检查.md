# Hibernate 脏检查

Created: November 18, 2024 11:43 AM
Tags: ORM

Hibernate的脏检查（dirty checking）。每次装载一个对象到内存时，session始终跟踪它的修改。于是每次对数据的查询，session都将跌代所有的session中的对象并检查脏数据，将脏数据flush到数据库。

`DefaultFlushEntityEventListener`

```java
	protected void dirtyCheck(final FlushEntityEvent event) throws HibernateException {
		int[] dirtyProperties = getDirtyProperties( event );
		event.setDatabaseSnapshot( null );
		if ( dirtyProperties == null ) {
			// do the dirty check the hard way
			dirtyProperties = performDirtyCheck( event );
		}
		else {
			// the Interceptor, SelfDirtinessTracker, or CustomEntityDirtinessStrategy
			// already handled the dirty check for us
			event.setDirtyProperties( dirtyProperties );
			event.setDirtyCheckHandledByInterceptor( true );
			event.setDirtyCheckPossible( true );
		}
		logDirtyProperties( event.getEntityEntry(), dirtyProperties );
	}
```

`*performDirtyCheck*( event )`  执行脏检查。

```java
private static int[] performDirtyCheck(FlushEntityEvent event) {
		 //-------------------
		try {
			session.getEventListenerManager().dirtyCalculationStart();
			// object loaded by update()
			final Object[] values = event.getPropertyValues();
			final Object[] loadedState = entry.getLoadedState();
			final Object entity = event.getEntity();
			if ( loadedState != null ) {
				// dirty check against the usual snapshot of the entity
				dirtyProperties = persister.findDirty( values, loadedState, entity, session );
				dirtyCheckPossible = true;
			}
			else if ( entry.getStatus() == Status.DELETED && !entry.isModifiableEntity() ) {
				// A non-modifiable (e.g., read-only or immutable) entity needs to be have
				// references to transient entities set to null before being deleted. No other
				// fields should be updated.
				if ( values != entry.getDeletedState() ) {
					throw new IllegalStateException(
							"Entity has status Status.DELETED but values != entry.getDeletedState"
					);
				}
				// Even if loadedState == null, we can dirty-check by comparing currentState and
				// entry.getDeletedState() because the only fields to be updated are those that
				// refer to transient entities that are being set to null.
				// - currentState contains the entity's current property values.
				// - entry.getDeletedState() contains the entity's current property values with
				//   references to transient entities set to null.
				// - dirtyProperties will only contain properties that refer to transient entities
				final Object[] currentState = persister.getValues( event.getEntity() );
				dirtyProperties = persister.findDirty( entry.getDeletedState(), currentState, entity, session );
				dirtyCheckPossible = true;
			}
			else {
				// dirty check against the database snapshot, if possible/necessary
				final Object[] databaseSnapshot = getDatabaseSnapshot( persister, entry.getId(), session );
				if ( databaseSnapshot != null ) {
					dirtyProperties = persister.findModified( databaseSnapshot, values, entity, session );
					dirtyCheckPossible = true;
					event.setDatabaseSnapshot( databaseSnapshot );
				}
				else {
					dirtyCheckPossible = false;
				}
			}
			event.setDirtyProperties( dirtyProperties );
			event.setDirtyCheckHandledByInterceptor( false );
			event.setDirtyCheckPossible( dirtyCheckPossible );
		}
		finally {
			eventManager.completeDirtyCalculationEvent( dirtyCalculationEvent, session, persister, entry, dirtyProperties );
			session.getEventListenerManager().dirtyCalculationEnd( dirtyProperties != null );
		}
		return dirtyProperties;
	}
```

`persister.findDirty( values, loadedState, entity, session )` 查找脏检查的属性。

`AbstractEntityPersister#findDirty`

`DirtyHelper.*findDirty`  这个脏检查帮助类查找属性，这里返回的是下标*

最终调用   `AbstractStandardBasicType#isDirty`  抽象标准基础类型的是否是脏数据的方法

```java
	public boolean isEqual(Object one, Object another) {
		if ( one == another ) {
			return true;
		}
		else if ( one == null || another == null ) {
			return false;
		}
		else {
			final AbstractClassJavaType<T> type = this.javaTypeAsAbstractClassJavaType;
			if ( type != null ) {
				//Optimize for the most common case: avoid the megamorphic call
				return type.areEqual( (T) one, (T) another );
			}
			else {
				return javaType.areEqual( (T) one, (T) another );
			}
		}
	}
```

调用`javaType`的`equal`方法

每种数据类型都会继承 `JavaType`  重写 `areEqual` 方法 ，判断两个对象是否相等

```java
	public boolean areEqual(T one, T another) {
		return Objects.equals( one, another );
	}
```

这里是直接使用的 `Objects.equals(one,another)` ,所以要准确的话，需要重写对象的 `equals` 方法

# 场景

使用`hibernate` 映射  `json` 对象，`json`对象没有重写 `equals` 方法，导致反序列对象每次都是最新，从而触发脏检查，每次查询都会 `update`。

如果使用`spring` 审计功能，每次`update`的时候还会触发审计更新，导致大量`update`语句，严重会导致死锁，久等待。