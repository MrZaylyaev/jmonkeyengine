/*
 * Copyright (c) 2003-2004, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.input.action;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

/**
 * <code>KeyRotateRightAction</code> performs the action of rotating a camera
 * a certain angle. This angle is determined by the speed at which the camera
 * can turn and the time between frames.
 * @author Mark Powell
 * @version $Id: KeyRotateRightAction.java,v 1.11 2004-08-22 02:00:34 cep21 Exp $
 */
public class KeyRotateRightAction extends AbstractInputAction {
    private static final Matrix3f incr=new Matrix3f();
    private Camera camera;
    private Vector3f lockAxis;

    /**
     * Constructor instantiates a new <code>KeyRotateLeftAction</code> object.
     * @param camera the camera to rotate.
     * @param speed the speed at which to rotate.
     */
    public KeyRotateRightAction(Camera camera, float speed) {
        this.camera = camera;
        this.speed = speed;
    }

    /**
     *
     * <code>setLockAxis</code> allows a certain axis to be locked, meaning
     * the camera will always be within the plane of the locked axis. For
     * example, if the camera is a first person camera, the user might lock
     * the camera's up vector. This will keep the camera vertical of the
     * ground.
     * @param lockAxis the axis to lock.
     */
    public void setLockAxis(Vector3f lockAxis) {
        this.lockAxis = lockAxis;
    }

    /**
     * <code>performAction</code> rotates the camera a certain angle.
     * @see com.jme.input.action.AbstractInputAction#performAction(float)
     */
    public void performAction(float time) {
        if(lockAxis == null) {
            incr.fromAxisAngle(camera.getUp(), -speed * time);
        } else {
            incr.fromAxisAngle(lockAxis, -speed * time);
        }
        incr.mult(camera.getUp(), camera.getUp());
        incr.mult(camera.getLeft(), camera.getLeft());
        incr.mult(camera.getDirection(), camera.getDirection());
        camera.update();
    }
}
