export function evaluateTests(){
    mocha.run()
        .on('end', function() {
            const progress = document.querySelector('#mocha .progress');
            if (!progress) return;
            const total = this.total;
            const passed = total - this.failures;
            const percent = Math.round((passed / total) * 100);

            progress.style.borderColor = this.failures > 0 ? '#dc3545' : '#28a745';

            progress.textContent = percent + '%';
            progress.style.fontSize = '15px';
        });
}