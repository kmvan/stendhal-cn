/***************************************************************************
 *                (C) Copyright 2003-2013 - Faiumoni e. V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.consumption;

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;

import java.lang.ref.WeakReference;

/**
 * timesout antidotes
 *
 * @author hendrik
 */
class AntidoteEater implements TurnListener {

	private WeakReference<RPEntity> entityReference;

	/**
	 * creates an antidote
	 *
	 * @param entity entity
	 */
	public AntidoteEater(final RPEntity entity) {
		entityReference = new WeakReference<RPEntity>(entity);
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		RPEntity entity = entityReference.get();
		
		if (entity == null) {
			return;
		}
		entity.removeImmunity("poison");
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AntidoteEater) {
			final AntidoteEater other = (AntidoteEater) obj;
			return entityReference.get().equals(other.entityReference.get());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return entityReference.hashCode();
	}
}
