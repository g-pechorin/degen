/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.OnActionCompleted;

public class Delay extends Action {
	static final ActionResetingPool<Delay> pool = new ActionResetingPool<Delay>(4, 100) {
		@Override
		protected Delay newObject () {
			return new Delay();
		}
	};

	protected Actor target;
	protected float taken;
	protected float duration;
	protected Action action;

	public static Delay $ (Action action, float duration) {
		Delay delay = pool.obtain();
		delay.duration = duration;
		delay.action = action;
		return delay;
	}
	
	public static Delay $(float duration) {
		Delay delay = pool.obtain();
		delay.duration = duration;
		delay.action = null;
		return delay;
	}

	@Override
	public void reset () {
		super.reset();
	}

	@Override
	public void setTarget (Actor actor) {
		if(action != null) action.setTarget(actor);
		this.target = actor;
		this.taken = 0;
	}

	@Override
	public void act (float delta) {
		taken += delta;
		if (taken > duration) {
			callActionCompletedListener();
			if(action != null) {
				action.act(delta);
				if (action.isDone()) action.callActionCompletedListener();
			}
		}
	}

	@Override
	public boolean isDone () {
		if(action != null) {
			return taken > duration && action.isDone();
		} else {
			return taken > duration;
		}
	}

	@Override
	public void finish () {
		pool.free(this);
		if(action != null) action.finish();
		super.finish();
	}

	@Override
	public Action copy () {
		if(action != null) {
			return $(action.copy(), duration);
		} else {
			return $(duration);
		}
	}

	@Override
	public Actor getTarget () {
		return target;
	}
	
	public static class MyDelay extends Delay
	{
		public MyDelay(String name, float duration)
		{
			m_name = name;
			this.duration = duration;
		}

		public String getName()
		{
			return m_name;
		}

		@Override
		public void finish()
		{
			super.finish();

			System.out.println(m_name + " has finished");
		}

		private String m_name;
	}
	
	public static void main (String[] args) {
		OnActionCompleted listener = new OnActionCompleted()
		{
			@Override
			public void completed(Action action)
			{
				MyDelay d = (MyDelay)action;
				System.out.println(d.getName() + " has completed");
			}
		};

		Action d1 = new MyDelay("Delay1", 1).setCompletionListener(listener);
		Action d2 = new MyDelay("Delay2", 1).setCompletionListener(listener);
		Action d3 = new MyDelay("Delay3", 2).setCompletionListener(listener);
		Sequence s = Sequence.$(d1, Parallel.$(d2, d3));
		
		while(!s.isDone()) {
			s.act(0.016f);
		}
		s.finish();
	}
}
