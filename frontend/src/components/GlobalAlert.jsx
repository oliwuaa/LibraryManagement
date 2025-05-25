import React, { useEffect, useState } from 'react';

const GlobalAlert = ({ message, type = 'info', onClose, duration = 3000 }) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        if (!message) return;

        setVisible(true);

        const timer = setTimeout(() => {
            setVisible(false);

            setTimeout(() => {
                onClose();
            }, 300);
        }, duration);

        return () => clearTimeout(timer);
    }, [message, duration, onClose]);

    if (!message) return null;

    const backgroundColors = {
        error: '#ef4444',
        success: '#10b981',
        info: '#3b82f6',
        warning: '#f59e0b'
    };

    return (
        <div
            style={{
                position: 'fixed',
                top: '70px',
                left: '50%',
                transform: 'translateX(-50%)',
                minWidth: '300px',
                maxWidth: '90vw',
                backgroundColor: backgroundColors[type] || backgroundColors.info,
                color: 'white',
                padding: '1rem 1.5rem',
                borderRadius: '8px',
                boxShadow: '0 4px 10px rgba(0, 0, 0, 0.3)',
                textAlign: 'center',
                zIndex: 1100,
                fontWeight: 'bold',
            }}
        >
            {message}
        </div>
    );

};

export default GlobalAlert;
